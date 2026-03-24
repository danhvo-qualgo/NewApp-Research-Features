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
import com.safeNest.demo.features.scamAnalyzer.impl.utils.PhishingLlmAnalyzer
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

    var isReady: Boolean = false
        private set

    private var systemPrompt: String = ""
    private var nativePtr: Long = 0L

    // JNI bridge — delegates to PhishingLlmAnalyzer which matches the
    // existing cpp/llm_jni.cpp JNI function names. Do NOT rename these;
    // the JNI symbol names are baked into the pre-built native library.
    private fun nativeCreate(configPath: String): Long =
        modelManager.analyzer.nativeCreate(configPath)

    private fun nativeGenerate(ptr: Long, prompt: String, listener: PhishingLlmAnalyzer.ProgressListener) =
        modelManager.analyzer.nativeGenerate(ptr, prompt, listener)

    private fun nativeRelease(ptr: Long) =
        modelManager.analyzer.nativeRelease(ptr)

    /**
     * Load the MNN LLM model. Call once at startup.
     *
     * @param systemPrompt The system prompt for the chat template.
     */
    fun load(systemPrompt: String) {
        this.systemPrompt = systemPrompt
        nativePtr = nativeCreate(modelConfigPath)
        isReady = nativePtr != 0L
        if (isReady) {
            Log.i("LMClient", "MNN LLM model loaded successfully")
        } else {
            Log.e("LMClient", "Failed to load MNN LLM model from: $modelConfigPath")
        }
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
        if (!isReady) return ""

        val fullPrompt = buildPrompt(userPrompt)

        // nativeGenerate is a blocking JNI call — run on IO dispatcher
        // to avoid blocking the calling coroutine's thread.
        return withContext(Dispatchers.IO) {
            completeBlocking(fullPrompt)
        }
    }

    private fun completeBlocking(fullPrompt: String): String {
        val fullOutput = StringBuilder()
        var parsedJSON: String? = null
        var insideThink = false
        // Pre-seed tracker: prefill already contributed "{" (depth 1)
        var jsonStarted = true
        var braceDepth = 1
        val jsonBuffer = StringBuilder(PREFILL)

        val listener = object : PhishingLlmAnalyzer.ProgressListener {
            override fun onProgress(chunk: String): Boolean {
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
                    return true // continue
                }
                if (insideThink) return true // continue

                // Accumulate characters and track brace depth
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
                                    return false // stop generation
                                }
                                jsonStarted = false
                                jsonBuffer.clear()
                            }
                        }

                        jsonStarted -> jsonBuffer.append(ch)
                    }
                }
                return true // continue
            }

            override fun onFinish() {
                // No-op — we handle completion in the caller
            }
        }

        nativeGenerate(nativePtr, fullPrompt, listener)

        // Return early-terminated JSON if found
        if (parsedJSON != null) return parsedJSON!!

        // Fallback: prepend prefill (since template consumed it) and return
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

    /**
     * Release native resources.
     */
    fun release() {
        if (nativePtr != 0L) {
            nativeRelease(nativePtr)
            nativePtr = 0L
            isReady = false
        }
    }
}
