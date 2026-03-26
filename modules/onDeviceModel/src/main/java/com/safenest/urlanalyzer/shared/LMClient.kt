/*
 * LMClient.kt — Shared on-device SLM wrapper using llama.cpp (via LlamaEngine).
 *
 * Mirrors iOS LMClient.swift. Used by both URL Analyzer and Text Analyzer.
 *
 * Features:
 *   - ChatML template with assistant-turn prefill: {"verdict":"
 *   - /no_think appended to user turn
 *   - Streaming with brace-depth tracking for early stop
 *   - Think-tag filtering
 *   - Repetition detection (sliding-window ngram)
 *   - Max accumulated length guard (2048 chars)
 *   - Partial JSON salvage when force-stopped
 */
package com.safenest.urlanalyzer.shared

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.text.iterator

class LMClient(
    private val engine: LlamaEngine,
    private val maxTokens: Int = 512
) {
    companion object {
        const val TAG = "LMClient"
        const val PREFILL = "{\"verdict\":\""
        const val MAX_ACCUMULATED_LEN = 2048
        const val REPEAT_WINDOW = 40
    }

    private var systemPrompt: String = ""

    fun load(systemPrompt: String) {
        this.systemPrompt = systemPrompt
        Log.i(TAG, "System prompt loaded (${systemPrompt.length} chars)")
    }

    fun getSystemPrompt(): String = systemPrompt

    private fun buildPrompt(userPrompt: String): String {
        return buildString {
            append("<|im_start|>system\n")
            append(systemPrompt)
            append("<|im_end|>\n")
            append("<|im_start|>user\n")
            append(userPrompt)
            append(" /no_think")
            append("<|im_end|>\n")
            append("<|im_start|>assistant\n")
            append(PREFILL)
        }
    }

    suspend fun complete(userPrompt: String): String {
        val fullPrompt = buildPrompt(userPrompt)
        return withContext(Dispatchers.IO) {
            completeBlocking(fullPrompt)
        }
    }

    suspend fun completeRaw(rawPrompt: String): String {
        return withContext(Dispatchers.IO) {
            completeBlocking(rawPrompt)
        }
    }

    private suspend fun completeBlocking(fullPrompt: String): String {
        val fullOutput = StringBuilder()
        var parsedJSON: String? = null
        var insideThink = false
        var jsonStarted = true
        var braceDepth = 1
        val jsonBuffer = StringBuilder(PREFILL)

        val listener = object : ProgressListener {
            override fun onProgress(chunk: String): Boolean {
                fullOutput.append(chunk)
                val output = fullOutput.toString()

                // Track <think> blocks
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
                    return true
                }
                if (insideThink) return true

                // Brace-depth tracking
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
                                    Log.d(TAG, "Valid JSON detected, stopping early")
                                    return false
                                }
                                jsonStarted = false
                                jsonBuffer.clear()
                            }
                        }
                        jsonStarted -> jsonBuffer.append(ch)
                    }
                }

                // Max length guard
                if (jsonBuffer.length > MAX_ACCUMULATED_LEN) {
                    Log.w(TAG, "Max length hit (${jsonBuffer.length}), force-stopping")
                    parsedJSON = salvagePartialJSON(jsonBuffer.toString())
                    return false
                }

                // Repetition detection
                if (jsonBuffer.length > REPEAT_WINDOW * 2) {
                    val tail = jsonBuffer.substring(jsonBuffer.length - REPEAT_WINDOW)
                    val before = jsonBuffer.substring(
                        jsonBuffer.length - REPEAT_WINDOW * 2,
                        jsonBuffer.length - REPEAT_WINDOW
                    )
                    if (tail == before) {
                        Log.w(TAG, "Repetition loop detected, force-stopping")
                        parsedJSON = salvagePartialJSON(jsonBuffer.toString())
                        return false
                    }
                }

                return true
            }

            override fun onFinish() {
                Log.d(TAG, "Generation finished")
            }
        }

        engine.generate(fullPrompt, maxTokens, listener)

        if (parsedJSON != null) return parsedJSON!!

        val raw = fullOutput.toString()
        val fallback = if (raw.trimStart().startsWith("{")) raw.trimStart() else PREFILL + raw
        return salvagePartialJSON(fallback)
    }

    private fun isValidResponse(json: String): Boolean {
        return try {
            val obj = JSONObject(json)
            obj.has("verdict") && obj.get("verdict") is String && obj.has("risk_score")
        } catch (e: Exception) {
            false
        }
    }

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

    private fun minimalFallback(raw: String): String {
        val verdict = Regex(""""verdict"\s*:\s*"([^"]+)"""").find(raw)
            ?.groupValues?.get(1) ?: "unknown"
        val risk = Regex(""""risk_score"\s*:\s*([\d.]+)""").find(raw)
            ?.groupValues?.get(1) ?: "0.5"
        return """{"verdict":"$verdict","risk_score":$risk,"key_findings":[]}"""
    }
}
