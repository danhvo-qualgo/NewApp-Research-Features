/*
 * URLAnalyzerOrchestrator.kt — Gate 1 → Gate 2 pipeline.
 */
package com.safenest.urlanalyzer.url

import com.safenest.urlanalyzer.shared.LMClient
import com.safenest.urlanalyzer.url.gate1.Gate1Classifier
import org.json.JSONArray
import org.json.JSONObject

data class URLAnalysisResult(
    val url: String,
    val verdict: String,
    val riskScore: Double,
    val keyFindings: List<URLKeyFinding>,
    val gate1Verdict: String,
    val gate1Score: Double,
    val gate1Time: Double,
    val gate2Verdict: String?,
    val gate2Score: Double?,
    val gate2Time: Double?,
    val rawResponse: String?,
    val elapsed: Double
)

data class URLKeyFinding(
    val category: String,
    val description: String
)

class URLAnalyzerOrchestrator(
    private val gate1: Gate1Classifier,
    private val lmClient: LMClient,
    private val gate2SystemPrompt: String
) {
    suspend fun analyze(url: String): URLAnalysisResult {
        val start = System.nanoTime()

        // Gate 1
        val g1Start = System.nanoTime()
        val g1 = gate1.classify(url)
        val g1Time = (System.nanoTime() - g1Start) / 1_000_000_000.0

        // Decision: skip Gate 2 if safe or has clear findings
        if (g1.verdict == "safe") {
            val elapsed = (System.nanoTime() - start) / 1_000_000_000.0
            return URLAnalysisResult(
                url = url, verdict = "safe", riskScore = 0.0,
                keyFindings = g1.keyFindings.map { URLKeyFinding(it.category, it.description) },
                gate1Verdict = g1.verdict, gate1Score = g1.riskScore, gate1Time = g1Time,
                gate2Verdict = null, gate2Score = null, gate2Time = null,
                rawResponse = null, elapsed = elapsed
            )
        }

        // Gate 2 — LLM analysis
        lmClient.load(gate2SystemPrompt)
        val g2Start = System.nanoTime()

        val g1FindingsStr = g1.keyFindings.joinToString(", ") { it.description }.ifBlank { "none" }
        val userPrompt = """URL: $url
Gate 1 verdict: ${g1.verdict} (risk: ${String.format("%.2f", g1.riskScore)})
Gate 1 findings: $g1FindingsStr
Analyze this URL. Only report findings based on provided signals. Do not invent findings."""

        val rawJson = lmClient.complete(userPrompt)
        val g2Time = (System.nanoTime() - g2Start) / 1_000_000_000.0
        val elapsed = (System.nanoTime() - start) / 1_000_000_000.0

        // Parse Gate 2 result
        val g2 = parseGate2Response(rawJson)

        // Combine: Gate 2 verdict takes precedence
        return URLAnalysisResult(
            url = url,
            verdict = g2.first,
            riskScore = g2.second,
            keyFindings = g2.third,
            gate1Verdict = g1.verdict, gate1Score = g1.riskScore, gate1Time = g1Time,
            gate2Verdict = g2.first, gate2Score = g2.second, gate2Time = g2Time,
            rawResponse = rawJson, elapsed = elapsed
        )
    }

    private fun parseGate2Response(rawJson: String): Triple<String, Double, List<URLKeyFinding>> {
        return try {
            val obj = JSONObject(rawJson)
            val verdict = obj.optString("verdict", "unknown")
            val riskScore = obj.optDouble("risk_score", 0.5).coerceIn(0.0, 1.0)
            val findings = mutableListOf<URLKeyFinding>()
            val kfArray = obj.optJSONArray("key_findings") ?: JSONArray()
            for (i in 0 until kfArray.length()) {
                val kf = kfArray.getJSONObject(i)
                findings.add(URLKeyFinding(
                    category = kf.optString("category", ""),
                    description = kf.optString("description", "").take(200)
                ))
            }
            Triple(verdict, riskScore, findings)
        } catch (e: Exception) {
            Triple("unknown", 0.5, emptyList())
        }
    }
}
