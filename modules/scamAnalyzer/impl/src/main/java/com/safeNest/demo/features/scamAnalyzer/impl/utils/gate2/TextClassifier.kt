/*
 * TextAnalyzerClassifier.kt — Text message scam classifier.
 *
 * Thin orchestrator that loads a contrastive prompt config and delegates
 * inference to the shared LMClient (same MNN engine used by URLAnalyzer Gate 2).
 *
 * LMClient already handles:
 *   - PREFILL JSON: assistant turn starts with '{"verdict":"' — forces JSON output
 *   - Streaming brace-depth tracking with early stop on valid JSON
 *   - Repetition detection (sliding-window ngram) with force-stop
 *   - Max accumulated length guard (2048 chars)
 *   - Partial JSON salvage when force-stopped
 *   - <think> block skipping
 *
 * This classifier:
 *   1. Loads its own system prompt (contrastive few-shot)
 *   2. Builds the full ChatML prompt with /no_think + </no_think> + PREFILL
 *   3. Calls lmClient.completeRaw() which returns prefill-prepended JSON
 *   4. Parses the JSON into TextAnalysisResult
 *   5. Truncates any overly-long descriptions (post-parse safety net)
 *
 * Model: Qwen3.5 0.8B (Q5_K_M, 563MB) — shared with URLAnalyzer.
 *
 * Prompt template (raw ChatML):
 *   <|im_start|>system\n{system_prompt}<|im_end|>
 *   <|im_start|>user\n{user_prompt} /no_think<|im_end|>
 *   <|im_start|>assistant\n</no_think>\n{"verdict":"
 *
 * The /no_think at end of user turn tells Qwen3 to skip reasoning.
 * The </no_think> at start of assistant turn closes any think block
 * the model might open despite the hint (MNN variant safety).
 * PREFILL then forces the model straight into JSON output.
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
    companion object {
        /** Max description length before truncation (post-parse safety net). */
        private const val MAX_DESCRIPTION_LEN = 200
    }

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
     * prepends </no_think> in the assistant turn to close any think block
     * the MNN model might open, then includes PREFILL to force JSON output.
     *
     * Passes the raw prompt to LMClient.completeRaw() which handles:
     *   - Streaming via MNN ProgressListener
     *   - Brace-depth tracking from PREFILL_DEPTH=1
     *   - Repetition detection (sliding-window ngram match)
     *   - Max accumulated length guard (2048 chars)
     *   - Early stop on valid JSON with verdict + risk_score
     *   - Partial JSON salvage when force-stopped
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
        // Prepend </no_think> in assistant turn to close any think block (MNN safety).
        // Append PREFILL at assistant turn to force JSON output.
        val fullPrompt = buildPrompt(userPrompt)

        // LMClient.completeRaw() takes our pre-built ChatML prompt as-is
        // (skips LMClient.buildPrompt()). Returns prefill-prepended JSON string.
        val rawJson = lmClient.completeRaw(fullPrompt)
        return parseResponse(rawJson)
    }

    /**
     * Build ChatML prompt with TextAnalyzer's system prompt.
     *
     * Template:
     *   <|im_start|>system\n{system_prompt}<|im_end|>
     *   <|im_start|>user\n{user_prompt} /no_think<|im_end|>
     *   <|im_start|>assistant\n</no_think>\n{"verdict":"
     *
     * /no_think at end of user turn: tells Qwen3 to skip <think> block.
     * </no_think> at start of assistant turn: closes any think block the
     *   MNN model might still open (safety for unreliable /no_think on MNN).
     * PREFILL: forces model output directly into JSON.
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
     * LMClient already prepends PREFILL and extracts/salvages JSON.
     * This method additionally truncates any overly-long descriptions
     * as a post-parse safety net against model repetition that slipped
     * through the streaming guards.
     */
    private fun parseResponse(rawJson: String): TextAnalysisResult {
        return try {
            val obj = JSONObject(rawJson)
            val findings = mutableListOf<KeyFinding>()
            val kfArray = obj.optJSONArray("key_findings") ?: JSONArray()
            for (i in 0 until kfArray.length()) {
                val kf = kfArray.getJSONObject(i)
                val desc = kf.optString("description", "")
                findings.add(KeyFinding(
                    category = kf.optString("category", ""),
                    description = truncateRepetition(desc)
                ))
            }
            TextAnalysisResult(
                verdict = obj.optString("verdict", "unknown"),
                riskScore = obj.optDouble("risk_score", 0.5).coerceIn(0.0, 1.0),
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

    /**
     * Truncate a description string if it exceeds MAX_DESCRIPTION_LEN or
     * contains obvious repetition.
     *
     * Detects repetition by checking if any 20-char window repeats
     * consecutively, then truncates to the first occurrence.
     */
    private fun truncateRepetition(text: String): String {
        if (text.length <= MAX_DESCRIPTION_LEN) return text

        // Detect repeated phrases: look for a 20-char segment that repeats
        val windowSize = 20
        if (text.length > windowSize * 2) {
            for (start in 0..(text.length - windowSize * 2)) {
                val segment = text.substring(start, start + windowSize)
                val nextStart = start + windowSize
                if (nextStart + windowSize <= text.length) {
                    val nextSegment = text.substring(nextStart, nextStart + windowSize)
                    if (segment == nextSegment) {
                        // Found repetition — truncate to first occurrence
                        return text.substring(0, start + windowSize).trimEnd('.', ' ') + "."
                    }
                }
            }
        }

        // No repetition detected, just hard-truncate at max length
        return text.take(MAX_DESCRIPTION_LEN).trimEnd('.', ' ') + "."
    }
}
