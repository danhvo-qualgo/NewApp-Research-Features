/*
 * LMClient.kt — On-device SLM wrapper using llama.cpp Android bindings.
 *
 * Mirrors iOS LMClient.swift.
 * Wraps the Qwen3 0.6B GGUF model.
 * Temperature: 0.0.
 *
 * Streams output and stops generation as soon as a complete JSON
 * response is detected — skips <think> blocks without waiting.
 *
 * Integration options:
 *   1. llama.cpp Android example (JNI bindings)
 *      https://github.com/ggml-org/llama.cpp/tree/master/examples/llama.android
 *   2. llama.cpp via pre-built AAR
 *
 * The same Qwen3-0.6B-Q4_K_M.gguf file from the iOS build works here.
 */

package com.safenest.urlanalyzer.gate2

import android.util.Log
import org.json.JSONObject

class LMClient(
    private val ggufPath: String,
    private val maxTokens: Int = 1024
) {

    companion object {
        /** Prefill injected into the assistant turn to force JSON output. */
        const val PREFILL = "{\"verdict\":\""

        init {
            // Load llama.cpp native library
            // System.loadLibrary("llama")
        }
    }

    var isReady: Boolean = false
        private set

    private var systemPrompt: String = ""

    // JNI declarations — to be implemented with llama.cpp bindings
    // private external fun nativeLoadModel(path: String, temp: Float, maxTokens: Int): Long
    // private external fun nativeFreeModel(modelPtr: Long)
    // private external fun nativeGenerate(modelPtr: Long, prompt: String, callback: (String) -> Unit)
    // private external fun nativeStop(modelPtr: Long)

    /**
     * Validate model can load. Call once at startup.
     */
    fun load(systemPrompt: String) {
        this.systemPrompt = systemPrompt
        // TODO: Test-load model to verify it works
        // val testPtr = nativeLoadModel(ggufPath, 0f, maxTokens)
        // isReady = testPtr != 0L
        // if (isReady) nativeFreeModel(testPtr)
        isReady = true // Placeholder
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
     * Streams output and stops as soon as valid JSON is detected.
     */
    suspend fun complete(userPrompt: String): String {
        if (!isReady) return ""

        val fullPrompt = buildPrompt(userPrompt)

        var fullOutput = StringBuilder()
        var parsedJSON: String? = null
        var insideThink = false
        // Pre-seed tracker: prefill already contributed "{" (depth 1)
        var jsonStarted = true
        var braceDepth = 1
        val jsonBuffer = StringBuilder(PREFILL)

        // TODO: Replace with actual llama.cpp JNI streaming call.
        // The callback pattern mirrors the iOS LLM.swift bot.update closure:
        //
        // val modelPtr = nativeLoadModel(ggufPath, 0f, maxTokens)
        // nativeGenerate(modelPtr, fullPrompt) { delta ->
        //     // This callback fires for each generated token
        //     fullOutput.append(delta)
        //     val output = fullOutput.toString()
        //
        //     // Track <think> blocks — skip content inside them
        //     if (output.endsWith("<think>") ||
        //         (output.contains("<think>") && !output.contains("</think>"))) {
        //         insideThink = true
        //     }
        //     if (output.contains("</think>")) {
        //         insideThink = false
        //         jsonStarted = false
        //         braceDepth = 0
        //         jsonBuffer.clear()
        //         return@nativeGenerate
        //     }
        //     if (insideThink) return@nativeGenerate
        //
        //     // Accumulate characters and track brace depth
        //     for (ch in delta) {
        //         when {
        //             ch == '{' -> {
        //                 jsonStarted = true
        //                 braceDepth++
        //                 jsonBuffer.append(ch)
        //             }
        //             ch == '}' && jsonStarted -> {
        //                 braceDepth--
        //                 jsonBuffer.append(ch)
        //                 if (braceDepth == 0) {
        //                     val candidate = jsonBuffer.toString()
        //                     if (isValidResponse(candidate)) {
        //                         parsedJSON = candidate
        //                         nativeStop(modelPtr)
        //                         return@nativeGenerate
        //                     }
        //                     jsonStarted = false
        //                     jsonBuffer.clear()
        //                 }
        //             }
        //             jsonStarted -> jsonBuffer.append(ch)
        //         }
        //     }
        // }
        // nativeFreeModel(modelPtr)

        Log.w("LMClient", "llama.cpp JNI not yet integrated — returning empty response")

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
}
