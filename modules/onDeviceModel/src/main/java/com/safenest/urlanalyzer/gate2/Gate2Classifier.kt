/*
 * Gate2Classifier.kt — On-device SLM classification.
 *
 * Mirrors iOS Gate2Classifier.swift and Python gate2/classifier.py.
 * Orchestrates: extract signals → build prompt → call SLM → parse JSON.
 */

package com.safenest.urlanalyzer.gate2

import android.util.Log
import com.safenest.urlanalyzer.AnalyzerData
import com.safenest.urlanalyzer.Gate1Result
import com.safenest.urlanalyzer.Gate2Result
import com.safenest.urlanalyzer.KeyFinding
import org.json.JSONObject

class Gate2Classifier(
    private val signalExtractor: SignalExtractor,
    private val promptBuilder: PromptBuilder,
    private val lmClient: LMClient
) {
    /** Last raw SLM response (for debugging). */
    var lastRawResponse: String = ""
        private set

    /**
     * Run Gate 2 classification.
     */
    suspend fun classify(
        url: String,
        gate1Result: Gate1Result,
        analyzerData: AnalyzerData
    ): Gate2Result {
        // 1. Extract signals
        val signals = signalExtractor.extract(analyzerData)

        // 2. Build prompt
        val userPrompt = promptBuilder.buildUserPrompt(
            url = url,
            gate1Result = gate1Result,
            signals = signals,
            analyzerData = analyzerData
        )

        // 3. Call SLM
        val rawResponse = lmClient.complete(userPrompt)
        lastRawResponse = rawResponse
        Log.d("Gate2", "Raw SLM response:\n$rawResponse")

        // 4. Parse response
        return parseResponse(rawResponse)
    }

    // MARK: - Response parsing

    private fun parseResponse(content: String): Gate2Result {
        var cleaned = content.trim()

        // Strip markdown code fences
        if (cleaned.startsWith("```")) {
            val firstNewline = cleaned.indexOf('\n')
            if (firstNewline >= 0) cleaned = cleaned.substring(firstNewline + 1)
            val lastFence = cleaned.lastIndexOf("```")
            if (lastFence >= 0) cleaned = cleaned.substring(0, lastFence)
            cleaned = cleaned.trim()
        }

        // Strip ALL thinking blocks (Qwen3 always generates <think>...</think>)
        while (true) {
            val thinkEnd = cleaned.indexOf("</think>")
            if (thinkEnd < 0) break
            cleaned = cleaned.substring(thinkEnd + "</think>".length).trim()
        }
        // Also strip any orphaned <think> opening tags
        val thinkStart = cleaned.indexOf("<think>")
        if (thinkStart >= 0) {
            cleaned = cleaned.substring(0, thinkStart).trim()
        }

        // Extract JSON between first { and last }
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start < 0 || end < 0 || end <= start) {
            return Gate2Result(verdict = "suspicious", riskScore = 0.5f, keyFindings = emptyList())
        }
        val jsonString = cleaned.substring(start, end + 1)

        return try {
            val json = JSONObject(jsonString)

            val validVerdicts = setOf("safe", "suspicious", "scam")
            var verdict = json.optString("verdict", "suspicious")
            if (verdict !in validVerdicts) verdict = "suspicious"

            val riskScore = json.optDouble("risk_score", 0.5)
                .toFloat().coerceIn(0f, 1f)

            val keyFindings = mutableListOf<KeyFinding>()
            val findingsArray = json.optJSONArray("key_findings")
            if (findingsArray != null) {
                for (i in 0 until findingsArray.length()) {
                    val f = findingsArray.getJSONObject(i)
                    keyFindings.add(
                        KeyFinding(
                            category = f.optString("category", "Unknown"),
                            description = f.optString("description", ""),
                            severity = f.optString("severity", "medium")
                        )
                    )
                }
            }

            Gate2Result(verdict = verdict, riskScore = riskScore, keyFindings = keyFindings)
        } catch (e: Exception) {
            Gate2Result(verdict = "suspicious", riskScore = 0.5f, keyFindings = emptyList())
        }
    }
}
