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
 * Includes repetition detection and max-length guards to handle MNN
 * model looping inside JSON string values. When force-stopped,
 * salvagePartialJSON() recovers a valid response from the truncated buffer.
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

        /** Max chars to accumulate before force-stopping. */
        const val MAX_ACCUMULATED_LEN = 2048

        /** Repetition window size for loop detection. */
        const val REPEAT_WINDOW = 40
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

    /** Read the current system prompt. */
    fun getSystemPrompt(): String = systemPrompt

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
        return withContext(Dispatchers.IO) {
            completeBlocking(fullPrompt)
        }
    }

    /**
     * Send a pre-built raw ChatML prompt and get the full response string.
     * Same streaming/early-stop logic as [complete], but skips [buildPrompt].
     */
    suspend fun completeRaw(rawPrompt: String): String {
        return withContext(Dispatchers.IO) {
            completeBlocking(rawPrompt)
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

        Log.d("LMClient", "Run complete blocking")
        val output = StringBuilder()

        val listener = object : PhishingLlmAnalyzer.ProgressListener {
            override fun onProgress(chunk: String): Boolean {
                output.append(chunk)
                fullOutput.append(chunk)
                val output = fullOutput.toString()

                // Track <think> / </no_think> blocks — reset JSON tracker
                if (output.endsWith("<think>") ||
                    (output.contains("<think>") && !output.contains("</think>"))
                ) {
                    insideThink = true
                }
                if (output.contains("</think>") || output.contains("</no_think>")) {
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
                                    Log.d("LMClient", "Stop response early")
                                    return false // stop generation
                                }
                                jsonStarted = false
                                jsonBuffer.clear()
                            }
                        }
                        jsonStarted -> jsonBuffer.append(ch)
                    }
                }

                // Guard: max length
                if (jsonBuffer.length > MAX_ACCUMULATED_LEN) {
                    Log.w("LMClient", "Max length hit (${jsonBuffer.length}), force-stopping")
                    parsedJSON = salvagePartialJSON(jsonBuffer.toString())
                    return false
                }

                // Guard: repetition detection
                if (jsonBuffer.length > REPEAT_WINDOW * 2) {
                    val tail = jsonBuffer.substring(jsonBuffer.length - REPEAT_WINDOW)
                    val before = jsonBuffer.substring(
                        jsonBuffer.length - REPEAT_WINDOW * 2,
                        jsonBuffer.length - REPEAT_WINDOW
                    )
                    if (tail == before) {
                        Log.w("LMClient", "Repetition loop detected, force-stopping")
                        parsedJSON = salvagePartialJSON(jsonBuffer.toString())
                        return false
                    }
                }

                return true // continue
            }

            override fun onFinish() {
                Log.d("LMClient", "Stop response normal")
            }
        }

        modelManager.analyzer.nativeGenerate(fullPrompt, listener)

        // Return early-terminated JSON if found
        if (parsedJSON != null) return parsedJSON!!

        // Fallback: if output already contains full JSON (e.g. after </no_think>),
        // don't prepend PREFILL. Otherwise prepend it.
        Log.d("LMClient", "Model output $output")
        val raw = fullOutput.toString()
        val fallback = if (raw.trimStart().startsWith("{")) raw.trimStart() else PREFILL + raw
        return salvagePartialJSON(fallback)
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
     * Recover valid JSON from a truncated or looping buffer.
     * Strips trailing repetition, closes open strings/arrays/braces.
     */
    private fun salvagePartialJSON(raw: String): String {
        var s = raw.trimEnd()

        // Strip trailing repetition
        if (s.length > REPEAT_WINDOW * 3) {
            val last = s.takeLast(REPEAT_WINDOW)
            val prev = s.substring(
                (s.length - REPEAT_WINDOW * 2).coerceAtLeast(0),
                (s.length - REPEAT_WINDOW).coerceAtLeast(0)
            )
            if (last == prev) {
                var cutoff = s.length - REPEAT_WINDOW
                while (cutoff > REPEAT_WINDOW &&
                    s.substring(cutoff - REPEAT_WINDOW, cutoff) == last
                ) {
                    cutoff -= REPEAT_WINDOW
                }
                s = s.substring(0, cutoff)
            }
        }

        // Close open string
        val quotes = s.count { it == '"' } - Regex("""\\"""").findAll(s).count()
        if (quotes % 2 != 0) {
            s = s.trimEnd('\\', ' ', '\n') + "\""
        }

        // Close open arrays and braces
        repeat((s.count { it == '[' } - s.count { it == ']' }).coerceAtLeast(0)) { s += "]" }
        repeat((s.count { it == '{' } - s.count { it == '}' }).coerceAtLeast(0)) { s += "}" }

        return try {
            val obj = JSONObject(s)
            if (obj.has("verdict")) s else minimalFallback(raw)
        } catch (e: Exception) {
            minimalFallback(raw)
        }
    }

    /** Last-resort fallback: extract verdict/risk via regex. */
    private fun minimalFallback(raw: String): String {
        val verdict = Regex(""""verdict"\s*:\s*"([^"]+)"""").find(raw)
            ?.groupValues?.get(1) ?: "unknown"
        val risk = Regex(""""risk_score"\s*:\s*([\d.]+)""").find(raw)
            ?.groupValues?.get(1) ?: "0.5"
        return """{"verdict":"$verdict","risk_score":$risk,"key_findings":[]}"""
    }
}
