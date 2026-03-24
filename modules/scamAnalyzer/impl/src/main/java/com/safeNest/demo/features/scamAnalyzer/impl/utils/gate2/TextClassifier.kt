/*
 * TextAnalyzerClassifier.kt — Text message scam classifier.
 *
 * Thin orchestrator that loads a contrastive prompt config and delegates
 * inference to the shared LMClient (same MNN engine used by URLAnalyzer Gate 2).
 *
 * LMClient already handles:
 *   - PREFILL JSON: assistant turn starts with '{"verdict":"' — forces JSON output
 *   - Streaming brace-depth tracking with early stop on valid JSON
 *   - <think> block skipping
 *
 * This classifier:
 *   1. Loads its own system prompt (contrastive few-shot)
 *   2. Builds the full ChatML prompt with /no_think + PREFILL
 *   3. Calls lmClient.completeRaw() which returns prefill-prepended JSON
 *   4. Parses the JSON into TextAnalysisResult
 *
 * Model: Qwen3.5 0.8B (Q5_K_M, 563MB) — shared with URLAnalyzer.
 */

package com.safeNest.demo.features.scamAnalyzer.impl.utils.gate2

import android.content.Context
import com.safenest.urlanalyzer.KeyFinding
import org.json.JSONArray
import org.json.JSONObject

data class TextAnalysisResult(
    val verdict: String,          // "scam" | "suspicious" | "safe"
    val riskScore: Double,        // 0.0 - 1.0
    val keyFindings: List<KeyFinding>,
    val rawResponse: String
)


class TextAnalyzerClassifier(
    private val lmClient: LMClient,
    context: Context
) {
    private val systemPrompt: String
    private val userTemplate: String

    init {
        val json = context.assets.open("text_analyzer_prompts.json")
            .bufferedReader().use { it.readText() }
        val config = JSONObject(json)
        systemPrompt = config.getString("system_prompt")
        userTemplate = config.getString("user_prompt_template")
    }

    /**
     * Analyze a text message for scam indicators.
     *
     * Builds a full ChatML prompt using TextAnalyzer's own system prompt
     * (contrastive few-shot), appends /no_think to disable reasoning,
     * and includes PREFILL to force JSON output.
     *
     * Passes the raw prompt to LMClient.completeRaw() which handles:
     *   - Streaming via MNN ProgressListener
     *   - Brace-depth tracking from PREFILL_DEPTH=1
     *   - Early stop on valid JSON with verdict + risk_score
     *   - <think> block skipping
     *
     * Uses completeRaw() instead of complete() because we build our own
     * ChatML prompt with a different system prompt than URLAnalyzer.
     *
     * @param message The raw message text to classify.
     * @return TextAnalysisResult with verdict, risk score, and key findings.
     */
    suspend fun analyze(message: String): TextAnalysisResult {
        val userPrompt = userTemplate.replace("{message}", message)

        // Build raw ChatML prompt with TextAnalyzer's system prompt.
        // Append /no_think to user turn to disable Qwen3 reasoning mode.
        // Append PREFILL at assistant turn to force JSON output.
        // This matches the Python prompt builder:
        //   <|im_start|>system\n{system_prompt}<|im_end|>
        //   <|im_start|>user\n{user_prompt} /no_think<|im_end|>
        //   <|im_start|>assistant\n{"verdict":"
        val fullPrompt = buildPrompt(userPrompt)

        // LMClient.completeRaw() takes our pre-built ChatML prompt as-is
        // (skips LMClient.buildPrompt()). Internally it streams tokens via
        // MNN's ProgressListener, tracks brace depth from PREFILL_DEPTH=1,
        // stops early when valid JSON with required fields is detected,
        // and returns the full prefill-prepended JSON string.
        val rawJson = lmClient.completeRaw(fullPrompt)
        return parseResponse(rawJson)
    }

    /**
     * Build ChatML prompt with TextAnalyzer's system prompt, /no_think, and PREFILL.
     */
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
            append(LMClient.PREFILL)
        }
    }

    /**
     * Parse the JSON string returned by LMClient into a TextAnalysisResult.
     *
     * LMClient already prepends PREFILL and extracts the first complete JSON
     * object via brace-depth tracking, so rawJson is ready to parse.
     */
    private fun parseResponse(rawJson: String): TextAnalysisResult {
        return try {
            val obj = JSONObject(rawJson)
            val findings = mutableListOf<KeyFinding>()
            val kfArray = obj.optJSONArray("key_findings") ?: JSONArray()
            for (i in 0 until kfArray.length()) {
                val kf = kfArray.getJSONObject(i)
                findings.add(KeyFinding(
                    category = kf.optString("category", ""),
                    description = kf.optString("description", "")
                ))
            }
            TextAnalysisResult(
                verdict = obj.optString("verdict", "unknown"),
                riskScore = obj.optDouble("risk_score", 0.5),
                keyFindings = findings,
                rawResponse = rawJson
            )
        } catch (e: Exception) {
            TextAnalysisResult(
                verdict = "unknown",
                riskScore = 0.5,
                keyFindings = emptyList(),
                rawResponse = rawJson
            )
        }
    }
}
