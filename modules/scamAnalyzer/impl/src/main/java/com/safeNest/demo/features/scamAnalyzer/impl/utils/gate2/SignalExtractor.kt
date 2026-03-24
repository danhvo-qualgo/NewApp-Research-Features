/*
 * SignalExtractor.kt — Config-driven signal extraction from analyzer data.
 *
 * Mirrors iOS SignalExtractor.swift and Python gate2/signal_extractor.py.
 * Loads gate2_signal_mappings.json, evaluates conditions against AnalyzerData.
 */

package com.safeNest.demo.features.scamAnalyzer.impl.utils.gate2

import android.content.Context
import com.safenest.urlanalyzer.*
import org.json.JSONArray
import org.json.JSONObject

class SignalExtractor(context: Context) {

    private val signalDefs: List<JSONObject>

    init {
        val jsonStr = context.assets.open("gate2_signal_mappings.json")
            .bufferedReader().use { it.readText() }
        val json = JSONObject(jsonStr)
        val signals = json.getJSONArray("signals")
        signalDefs = (0 until signals.length()).map { signals.getJSONObject(it) }
    }

    /**
     * Extract triggered signals from analyzer data.
     */
    fun extract(analyzerData: AnalyzerData): List<Gate2Signal> {
        val signals = mutableListOf<Gate2Signal>()

        for (sdef in signalDefs) {
            val sourceName = sdef.optString("source") ?: continue
            val field = sdef.optString("field") ?: continue
            val condition = sdef.optString("condition") ?: continue
            val signalName = sdef.optString("signalName") ?: continue
            val category = sdef.optString("category") ?: continue
            val severity = sdef.optString("severity") ?: continue
            val promptHint = sdef.optString("promptHint") ?: continue

            val sourceData = analyzerData.source(sourceName)
            if (sourceData.isEmpty()) continue

            val value = sourceData[field]

            if (!evaluateCondition(value, condition)) continue

            val description = interpolate(promptHint, sourceData)

            signals.add(
                Gate2Signal(
                    signalName = signalName,
                    category = category,
                    severity = severity,
                    description = description
                )
            )
        }

        return signals
    }

    // MARK: - Condition evaluation

    private fun evaluateCondition(value: Any?, condition: String): Boolean {
        val cond = condition.trim()

        // Special: isEmpty
        if (cond == "isEmpty") {
            return when (value) {
                null -> true
                is String -> value.isEmpty()
                is List<*> -> value.isEmpty()
                else -> false
            }
        }

        // Parse operator + expected
        val operators = listOf("==", "!=", ">=", "<=", ">", "<")
        for (op in operators) {
            if (cond.startsWith(op)) {
                val expectedStr = cond.removePrefix(op).trim()
                return compare(value, op, expectedStr)
            }
        }

        return false
    }

    private fun compare(value: Any?, op: String, expected: String): Boolean {
        // Boolean comparison
        if (expected == "true" || expected == "false") {
            val expectedBool = expected == "true"
            val valueBool = value as? Boolean ?: return false
            return when (op) {
                "==" -> valueBool == expectedBool
                "!=" -> valueBool != expectedBool
                else -> false
            }
        }

        // Numeric comparison
        val expectedNum = expected.toDoubleOrNull()
        if (expectedNum != null) {
            val valueNum = when (value) {
                is Double -> value
                is Float -> value.toDouble()
                is Int -> value.toDouble()
                is Long -> value.toDouble()
                else -> return false
            }

            return when (op) {
                "==" -> valueNum == expectedNum
                "!=" -> valueNum != expectedNum
                ">" -> valueNum > expectedNum
                "<" -> valueNum < expectedNum
                ">=" -> valueNum >= expectedNum
                "<=" -> valueNum <= expectedNum
                else -> false
            }
        }

        // String comparison
        val valueStr = value?.toString() ?: ""
        return when (op) {
            "==" -> valueStr == expected
            "!=" -> valueStr != expected
            else -> false
        }
    }

    // MARK: - Template interpolation

    private fun interpolate(template: String, data: Map<String, Any?>): String {
        var result = template
        val pattern = Regex("\\{(\\w+)\\}")
        result = pattern.replace(result) { match ->
            val key = match.groupValues[1]
            data[key]?.toString() ?: match.value
        }
        return result
    }
}
