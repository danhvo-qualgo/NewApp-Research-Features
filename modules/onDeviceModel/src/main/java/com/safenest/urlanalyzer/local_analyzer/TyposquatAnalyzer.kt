/*
 * TyposquatAnalyzer.kt — In-memory brand matching via Levenshtein distance.
 *
 * Mirrors iOS TyposquatAnalyzer.swift and Python local_analyzer/typosquat.py.
 */

package com.safenest.urlanalyzer.local_analyzer

import kotlin.math.abs
import kotlin.math.max

object TyposquatAnalyzer {

    private const val TYPOSQUAT_THRESHOLD = 0.3

    private val ccTLDSecondLevel = setOf("com", "co", "org", "net", "gov", "edu", "ac")

    /**
     * Analyze whether a domain typosquats a known brand.
     */
    fun analyze(
        domain: String,
        brandNames: List<String>,
        brandDomains: Set<String>
    ): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "isTyposquat" to false,
            "score" to 0.0,
            "matchedDomain" to "",
            "matchedBrand" to "",
            "distance" to 0
        )

        if (domain.isEmpty()) return result

        // Exact match → legitimate, not typosquat
        if (domain in brandDomains) return result

        // Extract base without TLD
        val parts = domain.split(".")
        val base = when {
            parts.size >= 3 && parts[parts.size - 2] in ccTLDSecondLevel ->
                parts.dropLast(2).joinToString(".")
            parts.size >= 2 ->
                parts.dropLast(1).joinToString(".")
            else -> domain
        }

        val baseClean = base
            .replace("-", "")
            .replace(".", "")
            .lowercase()

        if (baseClean.isEmpty()) return result

        var bestSim = 0.0
        var bestDist = 100
        var bestBrand = ""

        for (brand in brandNames) {
            // Containment check: "vietcombankverify" contains "vietcombank"
            if (baseClean.contains(brand) && brand.length >= 4) {
                val sim = brand.length.toDouble() / baseClean.length.toDouble()
                if (sim > bestSim) {
                    bestSim = maxOf(sim, 0.8) // containment = high confidence
                    bestDist = baseClean.length - brand.length
                    bestBrand = brand
                }
                continue
            }

            // Skip brands with very different lengths (for Levenshtein)
            if (abs(brand.length - baseClean.length) > 5) continue

            val dist = levenshtein(baseClean, brand)
            val sim = 1.0 - dist.toDouble() / max(baseClean.length, brand.length).coerceAtLeast(1).toDouble()

            if (sim > bestSim) {
                bestSim = sim
                bestDist = dist
                bestBrand = brand
            }
        }

        // Find the full domain for the matched brand
        var bestDomain = ""
        if (bestBrand.isNotEmpty()) {
            for (d in brandDomains) {
                if (d.split(".").firstOrNull() == bestBrand) {
                    bestDomain = d
                    break
                }
            }
        }

        if (bestSim >= TYPOSQUAT_THRESHOLD && bestDomain != domain) {
            result["isTyposquat"] = true
            result["score"] = (bestSim * 10000).toLong() / 10000.0
            result["matchedDomain"] = bestDomain
            result["matchedBrand"] = bestBrand
            result["distance"] = bestDist
        }

        return result
    }

    // MARK: - Levenshtein

    fun levenshtein(a: String, b: String): Int {
        val aLen = a.length
        val bLen = b.length

        if (aLen == 0) return bLen
        if (bLen == 0) return aLen

        var prev = IntArray(bLen + 1) { it }

        for (i in 0 until aLen) {
            val cur = IntArray(bLen + 1)
            cur[0] = i + 1
            for (j in 0 until bLen) {
                val cost = if (a[i] == b[j]) 0 else 1
                cur[j + 1] = minOf(cur[j] + 1, prev[j + 1] + 1, prev[j] + cost)
            }
            prev = cur
        }
        return prev[bLen]
    }
}
