package com.safeNest.demo.features.scamAnalyzer.impl.utils.gate2

import com.safenest.urlanalyzer.Gate1Result
import com.safenest.urlanalyzer.ResultCombiner
import com.safenest.urlanalyzer.URLAnalysisResult
import com.safenest.urlanalyzer.gate1.Gate1Classifier
import com.safenest.urlanalyzer.local_analyzer.LocalURLAnalyzer

class URLAnalyzerOrchestrator(
    private val gate1: Gate1Classifier,
    private val gate2: Gate2Classifier,
    private val lmClient: LMClient,
    private val localAnalyzer: LocalURLAnalyzer,
    private val combiner: ResultCombiner = ResultCombiner(),
    private val gate2Threshold: Float = 0.2f
) {

    val isModelReady: Boolean get() = lmClient.isReady

    // MARK: - Public API

    /**
     * Analyze a URL through the full Gate 1 → Gate 2 pipeline.
     */
    suspend fun analyze(url: String): URLAnalysisResult {
        val start = System.nanoTime()

        // 1. Run Gate 1 (~8ms)
        val gate1Result = gate1.classify(url)

        // 2. Decision logic
        if (gate1Result.verdict == "safe") {
            val elapsed = (System.nanoTime() - start) / 1_000_000.0
            return URLAnalysisResult(
                verdict = "safe",
                riskScore = gate1Result.riskScore,
                keyFindings = emptyList(),
                responseTime = "${elapsed.toLong()}ms",
                timestamp = System.currentTimeMillis(),
                source = "gate1",
                gate1Verdict = "safe",
                gate2Verdict = null,
                gate2RiskScore = null,
                gate2KeyFindings = null,
                gate2RawResponse = null
            )
        }

        if (gate1Result.verdict == "suspicious") {
            if (gate1Result.keyFindings.isNotEmpty()) {
                // Gate 1 can explain why → fast return
                val elapsed = (System.nanoTime() - start) / 1_000_000.0
                return URLAnalysisResult(
                    verdict = "suspicious",
                    riskScore = gate1Result.riskScore,
                    keyFindings = gate1Result.keyFindings,
                    responseTime = "${elapsed.toLong()}ms",
                    timestamp = System.currentTimeMillis(),
                    source = "gate1",
                    gate1Verdict = "suspicious",
                    gate2Verdict = null,
                    gate2RiskScore = null,
                    gate2KeyFindings = null,
                    gate2RawResponse = null
                )
            }
            // Gate 1 says suspicious but can't explain → escalate to Gate 2
            return escalateToGate2(url, gate1Result, start)
        }

        // verdict == "scam"
        if (gate1Result.riskScore < gate2Threshold) {
            // Low confidence scam → return with keyFindings
            val elapsed = (System.nanoTime() - start) / 1_000_000.0
            return URLAnalysisResult(
                verdict = "scam",
                riskScore = gate1Result.riskScore,
                keyFindings = gate1Result.keyFindings,
                responseTime = "${elapsed.toLong()}ms",
                timestamp = System.currentTimeMillis(),
                source = "gate1",
                gate1Verdict = "scam",
                gate2Verdict = null,
                gate2RiskScore = null,
                gate2KeyFindings = null,
                gate2RawResponse = null
            )
        }

        // High confidence scam → escalate to Gate 2
        return escalateToGate2(url, gate1Result, start)
    }

    /**
     * Run Gate 1 only (for quick testing without SLM).
     */
    fun classifyGate1Only(url: String): Gate1Result {
        return gate1.classify(url)
    }

    // MARK: - Gate 2 Escalation

    /**
     * Run local analyzer → Gate 2 SLM → combine with Gate 1.
     */
    private suspend fun escalateToGate2(
        url: String,
        gate1Result: Gate1Result,
        start: Long
    ): URLAnalysisResult {
        // Run local URL analyzer
        val analyzerData = localAnalyzer.analyze(url)

        // Run Gate 2 SLM classification
        val gate2Result = gate2.classify(url, gate1Result, analyzerData)

        // Combine Gate 1 + Gate 2 results
        val combined = combiner.combine(gate1Result, gate2Result)

        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        return URLAnalysisResult(
            verdict = combined.verdict,
            riskScore = combined.riskScore,
            keyFindings = combined.keyFindings,
            responseTime = "${elapsed.toLong()}ms",
            timestamp = System.currentTimeMillis(),
            source = "gate1_and_gate2",
            gate1Verdict = combined.gate1Verdict,
            gate2Verdict = combined.gate2Verdict,
            gate2RiskScore = gate2Result.riskScore,
            gate2KeyFindings = gate2Result.keyFindings,
            gate2RawResponse = gate2.lastRawResponse
        )
    }
}