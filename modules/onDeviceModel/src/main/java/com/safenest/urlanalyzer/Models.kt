/*
 * Models.kt — Shared data types for the URL Analyzer pipeline.
 *
 * Mirrors iOS Models.swift and Python models.py.
 */

package com.safenest.urlanalyzer

/**
 * Key finding — shared across Gate 1 and Gate 2.
 */
data class KeyFinding(
    val category: String,
    val description: String,
    val severity: String = "medium"
)

/**
 * Gate 1 result — internal, includes raw feature vector.
 */
data class Gate1Result(
    val verdict: String,          // "safe", "suspicious", "scam"
    val riskScore: Float,         // confidence 0.0–1.0
    val features: FloatArray,     // raw 33-float vector (LightGBM v1.0)
    val keyFindings: List<KeyFinding>,
    val responseTimeMs: Double
)

/**
 * Gate 2 result — SLM classification output.
 */
data class Gate2Result(
    val verdict: String,
    val riskScore: Float,
    val keyFindings: List<KeyFinding>
)

/**
 * Gate 2 signal — extracted from analyzer data, fed into SLM prompt.
 */
data class Gate2Signal(
    val signalName: String,
    val category: String,
    val severity: String,
    val description: String
)

/**
 * Local analyzer output — dict-of-dicts per analysis source.
 */
data class AnalyzerData(
    val homograph: Map<String, Any?>,
    val typosquat: Map<String, Any?>,
    val ssl: Map<String, Any?>,
    val scamDB: Map<String, Any?>,
    val pageInfo: Map<String, Any?>
) {
    fun source(name: String): Map<String, Any?> = when (name) {
        "homograph" -> homograph
        "typosquat" -> typosquat
        "ssl" -> ssl
        "scamDB" -> scamDB
        "pageInfo" -> pageInfo
        else -> emptyMap()
    }
}

/**
 * Combined Gate 1 + Gate 2 result.
 */
data class CombinedResult(
    val verdict: String,
    val riskScore: Float,
    val keyFindings: List<KeyFinding>,
    val gate1Verdict: String,
    val gate2Verdict: String
)

/**
 * Final URL analysis result — returned to the caller.
 */
data class URLAnalysisResult(
    val verdict: String,
    val riskScore: Float,
    val keyFindings: List<KeyFinding>,
    val responseTime: String,
    val timestamp: Long,
    val source: String,               // "gate1" or "gate1_and_gate2"
    val gate1Verdict: String?,
    val gate2Verdict: String?,
    val gate2RiskScore: Float? = null,
    val gate2KeyFindings: List<KeyFinding>? = null,
    val gate2RawResponse: String? = null
) {
    fun toResponse(): Map<String, Any?> = mapOf(
        "data" to mapOf(
            "verdict" to verdict,
            "riskScore" to riskScore,
            "keyFindings" to keyFindings.map { mapOf("category" to it.category, "description" to it.description) }
        ),
        "responseTime" to responseTime,
        "timestamp" to timestamp
    )
}
