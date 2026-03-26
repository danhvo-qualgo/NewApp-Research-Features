/*
 * TextAnalyzerClassifier.kt — SMS scam classifier using contrastive prompt.
 *
 * Reuses shared LMClient (llama.cpp). Loads prompt config from assets.
 */
package com.safenest.urlanalyzer.text

import android.content.Context
import com.safenest.urlanalyzer.shared.LMClient
import org.json.JSONArray
import org.json.JSONObject

data class TextAnalysisResult(
    val verdict: String,
    val riskScore: Double,
    val keyFindings: List<KeyFinding>,
    val rawResponse: String
)

data class KeyFinding(
    val category: String,
    val description: String
)

class TextAnalyzerClassifier(
    private val lmClient: LMClient,
    context: Context
) {
    private val systemPrompt: String
    private val userTemplate: String

    init {
        val json = context.assets.open("config/text_analyzer_prompts.json")
            .bufferedReader().use { it.readText() }
        val config = JSONObject(json)
        systemPrompt = config.getString("system_prompt")
        userTemplate = config.getString("user_prompt_template")
    }

    fun activate() {
        lmClient.load(systemPrompt)
    }

    suspend fun analyze(message: String, signalsSummary: String? = null): TextAnalysisResult {
        var userPrompt = userTemplate.replace("{message}", message)
        if (!signalsSummary.isNullOrBlank()) {
            userPrompt += "\n\n## Scam Signals\n$signalsSummary"
        }

        val fullPrompt = buildString {
            append("<|im_start|>system\n")
            append(systemPrompt)
            append("<|im_end|>\n")
            append("<|im_start|>user\n")
            append(userPrompt)
            append(" /no_think")
            append("<|im_end|>\n")
            append("<|im_start|>assistant\n")
            append(LMClient.PREFILL)
        }

        val rawJson = lmClient.completeRaw(fullPrompt)
        return parseResponse(rawJson)
    }

    private fun parseResponse(rawJson: String): TextAnalysisResult {
        return try {
            val obj = JSONObject(rawJson)
            val findings = mutableListOf<KeyFinding>()
            val kfArray = obj.optJSONArray("key_findings") ?: JSONArray()
            for (i in 0 until kfArray.length()) {
                val kf = kfArray.getJSONObject(i)
                findings.add(KeyFinding(
                    category = kf.optString("category", ""),
                    description = kf.optString("description", "").take(200)
                ))
            }
            TextAnalysisResult(
                verdict = obj.optString("verdict", "unknown"),
                riskScore = obj.optDouble("risk_score", 0.5).coerceIn(0.0, 1.0),
                keyFindings = findings,
                rawResponse = rawJson
            )
        } catch (e: Exception) {
            TextAnalysisResult("unknown", 0.5, emptyList(), rawJson)
        }
    }
}
