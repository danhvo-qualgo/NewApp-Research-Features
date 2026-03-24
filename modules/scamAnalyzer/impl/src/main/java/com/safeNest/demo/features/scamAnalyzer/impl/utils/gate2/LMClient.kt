/*
 * LMClient.kt — On-device SLM wrapper using MNN LLM engine.
 *
 * Mirrors iOS LMClient.swift.
 * Wraps the Qwen3 0.6B model via MNN's Llm C++ API (same engine as cpp/).
 * Temperature: 0.0.
 *
 * Streams output via the ProgressListener JNI callback pattern (matching
 * cpp/llm_jni.cpp's TokenStreamBuf). Stops generation as soon as a
 * complete JSON response is detected — skips <think> blocks.
 *
 * The native side is handled by cpp/llm_jni.cpp which provides:
 *   - nativeCreate(configPath)  → jlong (opaque Llm* pointer)
 *   - nativeGenerate(ptr, prompt, listener)
 *   - nativeRelease(ptr)
 *
 * The ProgressListener interface:
 *   - onProgress(chunk: String): Boolean  — return false to stop
 *   - onFinish()
 */

package com.safeNest.demo.features.scamAnalyzer.impl.utils.gate2

import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.impl.utils.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LMClient(
    private val modelConfigPath: String,
    private val modelManager: ModelManager,
    private val maxTokens: Int = 512
) {

    companion object {
        /** Prefill injected into the assistant turn to force JSON output. */
        const val PREFILL = "{\"verdict\":\""
    }

    private var systemPrompt: String = ""

    /**
     * Load the MNN LLM model. Call once at startup.
     *
     * @param systemPrompt The system prompt for the chat template.
     */
    fun load(systemPrompt: String) {
        this.systemPrompt = systemPrompt
        Log.i("LMClient", "MNN LLM model loaded successfully")
    }

    /**
     * Build the full ChatML prompt with prefill.
     */
    private fun buildPrompt(userPrompt: String): String {
        return buildString {
            append("<|im_start|>system\n")
            append(systemPrompt)
            append("<|im_end|>\n")
            append("<|im_start|>user\n")
            append(userPrompt)
            append("<|im_end|>\n")
            append("<|im_start|>assistant\n")
            append(PREFILL)
        }
    }

    /**
     * Send a prompt and get the full response string.
     * Streams output via MNN's TokenStreamBuf and stops as soon as valid JSON is detected.
     */
    suspend fun complete(userPrompt: String): String {
        val fullPrompt = buildPrompt(userPrompt)

        // nativeGenerate is a blocking JNI call — run on IO dispatcher
        // to avoid blocking the calling coroutine's thread.
        return withContext(Dispatchers.IO) {
            completeBlocking(fullPrompt)
        }
    }

    private suspend fun completeBlocking(fullPrompt: String): String {
        val fullOutput = StringBuilder()
        var parsedJSON: String? = null
        var insideThink = false

        // Pre-seed tracker: prefill already contributed "{"
        var jsonStarted = true
        var braceDepth = 1
        val jsonBuffer = StringBuilder(PREFILL)

        modelManager.analyzer.llmProcessing(fullPrompt).collect { chunk ->

            if (parsedJSON != null) return@collect

            fullOutput.append(chunk)
            val output = fullOutput.toString()

            // Track <think> blocks — skip content inside them
            if (output.endsWith("<think>") ||
                (output.contains("<think>") && !output.contains("</think>"))
            ) {
                insideThink = true
            }

            if (output.contains("</think>")) {
                insideThink = false
                jsonStarted = false
                braceDepth = 0
                jsonBuffer.clear()
                return@collect
            }

            if (insideThink) return@collect

            // Track JSON braces
            for (ch in chunk) {
                when {
                    ch == '{' -> {
                        jsonStarted = true
                        braceDepth++
                        jsonBuffer.append(ch)
                    }

                    ch == '}' && jsonStarted -> {
                        braceDepth--
                        jsonBuffer.append(ch)

                        if (braceDepth == 0) {
                            val candidate = jsonBuffer.toString()

                            if (isValidResponse(candidate)) {
                                parsedJSON = candidate
                                return@collect
                            }

                            jsonStarted = false
                            jsonBuffer.clear()
                        }
                    }

                    jsonStarted -> jsonBuffer.append(ch)
                }
            }
        }

        parsedJSON?.let { return it }

        return PREFILL + fullOutput.toString()
    }

    /**
     * Check if a JSON string has the required fields.
     */
    private fun isValidResponse(json: String): Boolean {
        return try {
            val obj = JSONObject(json)
            obj.has("verdict") && obj.get("verdict") is String && obj.has("risk_score")
        } catch (e: Exception) {
            false
        }
    }
}
