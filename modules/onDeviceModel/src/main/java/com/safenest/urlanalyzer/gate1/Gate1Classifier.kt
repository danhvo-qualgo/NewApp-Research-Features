/*
 * Gate1Classifier.kt — SafeNest Gate 1 URL Safety Classifier for Android.
 *
 * LightGBM v1.0 — 33 engineered features, no CNN branch.
 * Uses ONNX Runtime for inference and JNI for C feature extraction.
 * Includes keyFindings mapping from the 33-float feature vector.
 *
 * Mirrors iOS Gate1Classifier.swift.
 *
 * Dependencies:
 *   1. ONNX Runtime Android (com.microsoft.onnxruntime:onnxruntime-android)
 *   2. gate1_features.c compiled via NDK (JNI bridge)
 *   3. brands.bin + brands.csv in assets/
 *   4. gate1_keyfinding_mappings.json in assets/
 */

package com.safenest.urlanalyzer.gate1

import ai.onnxruntime.OnnxMap
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.safenest.urlanalyzer.Gate1Result
import com.safenest.urlanalyzer.KeyFinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.FloatBuffer

class Gate1Classifier(
    private val context: Context,
    private val threshold: Float = 0.2f,
    private val suspiciousConfidence: Float = 0.5f
) : AutoCloseable {

    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private var brandsPtr: Long = 0L

    private val keyFindingRules: List<KeyFindingRule>

    /** Brand names extracted from CSV — exposed for LocalURLAnalyzer. */
    var brandNames: List<String> = emptyList()
        private set

    /** Brand domains extracted from CSV — exposed for LocalURLAnalyzer. */
    var brandDomains: Set<String> = emptySet()
        private set

    init {
        // Load ONNX model from assets
        val modelBytes = context.assets.open("gate1_lightgbm.onnx").readBytes()
        session = ortEnv.createSession(modelBytes)

        // Load brands.bin via JNI (for C-side allowlist + feature extraction)
        val brandsBytes = context.assets.open("brands.bin").readBytes()
        brandsPtr = nativeLoadBrands(brandsBytes)
        require(brandsPtr != 0L) { "Failed to load brands.bin" }

        // Load brands.csv for Kotlin-side brand matching (LocalURLAnalyzer)
        loadBrandsFromCSV()

        // Load keyfinding mappings
        keyFindingRules = loadKeyFindingRules()
    }

    companion object {
        init {
            System.loadLibrary("gate1_features")
        }
    }

    // ── JNI Native Methods ──

    private external fun nativeLoadBrands(data: ByteArray): Long
    private external fun nativeFreeBrands(ptr: Long)
    private external fun nativeIsAllowlisted(url: String, brandsPtr: Long): Boolean
    private external fun nativeExtractFeatures(url: String, brandsPtr: Long): FloatArray

    // ── Classification ──

    /**
     * Classify a URL. Returns Gate1Result for pipeline integration.
     */
    fun classify(url: String): Gate1Result {
        val start = System.nanoTime()

        // 1. Allowlist gate (native)
        if (brandsPtr != 0L && nativeIsAllowlisted(url, brandsPtr)) {
            val elapsed = (System.nanoTime() - start) / 1_000_000.0
            return Gate1Result(
                verdict = "safe",
                riskScore = 1.0f,
                features = FloatArray(0),
                keyFindings = emptyList(),
                responseTimeMs = elapsed
            )
        }

        // 2. Extract features (C library via JNI → 33 floats)
        val features = if (brandsPtr != 0L) {
            nativeExtractFeatures(url, brandsPtr)
        } else {
            FloatArray(33)
        }

        // 3. ONNX Runtime inference — LightGBM outputs probabilities directly
        val featTensor = OnnxTensor.createTensor(
            ortEnv, FloatBuffer.wrap(features), longArrayOf(1, 33)
        )

        val results = session.run(mapOf("features" to featTensor))

        // LightGBM ONNX outputs: [labels, probabilities]
        // probabilities is a list of OnnxMap objects: [{0: p0, 1: p1}]
        @Suppress("UNCHECKED_CAST")
        val probMaps = results[1].value as List<OnnxMap>
        val onnxMap = probMaps[0]
        
        // Extract probability for class 1 (scam) from OnnxMap using getValue()
        val mapData = onnxMap.value as Map<*, *>
        val probability = mapData[1L] as? Float ?: 0.0f

        featTensor.close()
        results.close()

        // 4. Compute confidence (threshold-relative)
        val isScam = probability >= threshold
        val confidence = if (isScam) {
            ((probability - threshold) / (1.0f - threshold)).coerceIn(0f, 1f)
        } else {
            ((threshold - probability) / threshold).coerceIn(0f, 1f)
        }

        // 5. Determine verdict
        val verdict = when {
            confidence < suspiciousConfidence -> "suspicious"
            isScam -> "scam"
            else -> "safe"
        }

        // 6. Map features to key findings (only when not safe)
        val keyFindings = if (verdict != "safe") {
            mapKeyFindings(features)
        } else {
            emptyList()
        }

        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        return Gate1Result(
            verdict = verdict,
            riskScore = confidence,
            features = features,
            keyFindings = keyFindings,
            responseTimeMs = elapsed
        )
    }

    override fun close() {
        if (brandsPtr != 0L) {
            nativeFreeBrands(brandsPtr)
            brandsPtr = 0L
        }
        session.close()
    }

    // ── KeyFinding Mapping ──

    private data class KeyFindingRule(
        val featureIndex: Int,
        val op: (Float, Float) -> Boolean,
        val threshold: Float,
        val category: String,
        val description: String,
        val severity: String
    )

    private fun mapKeyFindings(features: FloatArray): List<KeyFinding> {
        val findings = mutableListOf<KeyFinding>()
        val seenCategories = mutableSetOf<String>()

        for (rule in keyFindingRules) {
            if (rule.featureIndex >= features.size) continue
            if (rule.category !in seenCategories &&
                rule.op(features[rule.featureIndex], rule.threshold)
            ) {
                seenCategories.add(rule.category)
                findings.add(
                    KeyFinding(
                        category = rule.category,
                        description = rule.description,
                        severity = rule.severity
                    )
                )
            }
        }
        return findings
    }

    private fun loadKeyFindingRules(): List<KeyFindingRule> {
        return try {
            val jsonStr = context.assets.open("gate1_keyfinding_mappings.json")
                .bufferedReader().use { it.readText() }
            val json = JSONObject(jsonStr)
            val mappings = json.getJSONArray("mappings")
            val rules = mutableListOf<KeyFindingRule>()

            for (i in 0 until mappings.length()) {
                val m = mappings.getJSONObject(i)
                val index = m.getInt("featureIndex")
                val condition = m.getString("condition")
                val category = m.getString("category")
                val desc = m.getString("description")
                val severity = m.optString("severity", "medium")

                val parsed = parseCondition(condition) ?: continue
                rules.add(
                    KeyFindingRule(
                        featureIndex = index,
                        op = parsed.first,
                        threshold = parsed.second,
                        category = category,
                        description = desc,
                        severity = severity
                    )
                )
            }
            rules
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseCondition(cond: String): Pair<(Float, Float) -> Boolean, Float>? {
        val trimmed = cond.trim()
        val operators = listOf(
            ">=" to { a: Float, b: Float -> a >= b },
            "<=" to { a: Float, b: Float -> a <= b },
            "==" to { a: Float, b: Float -> a == b },
            "!=" to { a: Float, b: Float -> a != b },
            ">" to { a: Float, b: Float -> a > b },
            "<" to { a: Float, b: Float -> a < b }
        )
        for ((sym, op) in operators) {
            if (trimmed.startsWith(sym)) {
                val valStr = trimmed.removePrefix(sym).trim()
                val value = valStr.toFloatOrNull() ?: continue
                return Pair(op, value)
            }
        }
        return null
    }

    // ── Brand Data (Kotlin-side, for LocalURLAnalyzer) ──

    private fun loadBrandsFromCSV() {
        try {
            val reader = BufferedReader(
                InputStreamReader(context.assets.open("brands.csv"))
            )
            val lines = reader.readLines()
            reader.close()

            if (lines.size < 2) return

            val header = lines[0].split(",").map { it.trim().lowercase() }
            val domainIdx = header.indexOf("domain")
            if (domainIdx < 0) return

            val names = mutableListOf<String>()
            val nameSet = mutableSetOf<String>()
            val domains = mutableSetOf<String>()

            for (line in lines.drop(1)) {
                val cols = line.split(",").map { it.trim() }
                if (domainIdx >= cols.size) continue
                val domain = cols[domainIdx].lowercase()
                if (domain.isEmpty()) continue

                domains.add(domain)

                val base = domain.split(".").firstOrNull() ?: ""
                if (base.length >= 3 && base !in nameSet) {
                    nameSet.add(base)
                    names.add(base)
                }
            }

            brandNames = names
            brandDomains = domains
        } catch (e: Exception) {
            // Brands not available — continue without typosquat detection
        }
    }
}
