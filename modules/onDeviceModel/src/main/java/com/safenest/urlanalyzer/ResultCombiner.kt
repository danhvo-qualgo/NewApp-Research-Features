/*
 * ResultCombiner.kt — Fuses Gate 1 + Gate 2 results.
 *
 * Mirrors iOS ResultCombiner.swift and Python result_combiner.py.
 * Gate 2 does NOT veto Gate 1. Verdict matrix + weighted risk score.
 */

package com.safenest.urlanalyzer

class ResultCombiner(
    private val gate1Weight: Float = 0.4f,
    private val gate2Weight: Float = 0.6f
) {

    // Verdict combination matrix
    private val verdictMatrix = mapOf(
        "scam" to mapOf(
            "scam" to "scam",
            "suspicious" to "scam",
            "safe" to "suspicious"
        ),
        "suspicious" to mapOf(
            "scam" to "scam",
            "suspicious" to "suspicious",
            "safe" to "suspicious"
        )
    )

    fun combine(gate1: Gate1Result, gate2: Gate2Result): CombinedResult {
        // Determine combined verdict
        val verdict = verdictMatrix[gate1.verdict]?.get(gate2.verdict) ?: "suspicious"

        // Weighted risk score
        val riskScore = (gate1Weight * gate1.riskScore + gate2Weight * gate2.riskScore)
            .coerceIn(0f, 1f)

        // Merge key findings, deduplicate by category (prefer Gate 2)
        val findings = mergeFindings(gate1.keyFindings, gate2.keyFindings)

        return CombinedResult(
            verdict = verdict,
            riskScore = riskScore,
            keyFindings = findings,
            gate1Verdict = gate1.verdict,
            gate2Verdict = gate2.verdict
        )
    }

    private fun mergeFindings(
        gate1: List<KeyFinding>,
        gate2: List<KeyFinding>
    ): List<KeyFinding> {
        val seenCategories = mutableSetOf<String>()
        val merged = mutableListOf<KeyFinding>()

        // Gate 2 findings take priority
        for (f in gate2) {
            seenCategories.add(f.category)
            merged.add(f)
        }
        for (f in gate1) {
            if (f.category !in seenCategories) {
                seenCategories.add(f.category)
                merged.add(f)
            }
        }
        return merged
    }
}
