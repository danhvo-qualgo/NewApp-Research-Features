/*
 * Gate1Classifier.kt — SafeNest Gate 1 URL Safety Classifier for Android.
 *
 * Uses ONNX Runtime Mobile (or TFLite) for inference and JNI for C feature extraction.
 * Includes keyFindings mapping from the 30-float feature vector.
 *
 * Mirrors iOS Gate1Classifier.swift.
 *
 * Dependencies:
 *   1. ONNX Runtime Android (com.microsoft.onnxruntime:onnxruntime-android)
 *      — OR TFLite (org.tensorflow:tensorflow-lite)
 *   2. gate1_features.c compiled via NDK (JNI bridge)
 *   3. brands.bin + brands.csv in assets/
 *   4. gate1_keyfinding_mappings.json in assets/
 */

package com.safenest.urlanalyzer.gate1

import android.content.Context
import com.safenest.urlanalyzer.Gate1Result
import com.safenest.urlanalyzer.KeyFinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class Gate1Classifier(
    private val context: Context,
    private val threshold: Float = 0.2f,
    private val suspiciousConfidence: Float = 0.5f
) {

    // TODO: Replace with actual ONNX Runtime or TFLite model instance
    // private lateinit var session: OrtSession
    // private lateinit var env: OrtEnvironment

    private val keyFindingRules: List<KeyFindingRule>
    var brandNames: List<String> = emptyList()
        private set
    var brandDomains: Set<String> = emptySet()
        private set

    init {
        // Load keyfinding mappings
        keyFindingRules = loadKeyFindingRules()

        // Load brand data from CSV
        loadBrandsFromCSV()

        // Load ONNX model
        loadModel()
    }

    companion object {
        init {
            // Load native C library (gate1_features via JNI)
            System.loadLibrary("gate1_features")
        }
    }

    // JNI declarations — implemented in gate1_jni.c
    private external fun nativeLoadBrands(assetData: ByteArray): Long
    private external fun nativeFreeBrands(brandsPtr: Long)
    private external fun nativeIsAllowlisted(url: String, brandsPtr: Long): Boolean
    private external fun nativeExtractFeatures(url: String, brandsPtr: Long): FloatArray
    private external fun nativeEncodeUrl(url: String): IntArray

    private var brandsPtr: Long = 0L

    private fun loadModel() {
        // Load brands.bin via JNI
        val brandsData = context.assets.open("brands.bin").use { it.readBytes() }
        brandsPtr = nativeLoadBrands(brandsData)

        // TODO: Initialize ONNX Runtime
        // env = OrtEnvironment.getEnvironment()
        // val modelBytes = context.assets.open("gate1_hybrid.onnx").use { it.readBytes() }
        // session = env.createSession(modelBytes)
    }

    fun classify(url: String): Gate1Result {
        val start = System.nanoTime()

        // 1. Allowlist gate
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

        // 2. Extract features (C library via JNI)
        val featureArray = if (brandsPtr != 0L) {
            nativeExtractFeatures(url, brandsPtr)
        } else {
            FloatArray(30)
        }

        // 3. Encode URL for CNN branch
        val urlIds = nativeEncodeUrl(url)

        // 4. Model inference
        // TODO: Run ONNX or TFLite inference
        // For now, use feature-based heuristic as placeholder
        val probability = runInference(featureArray, urlIds)

        // 5. Compute confidence and verdict
        val isScam = probability >= threshold
        val confidence: Float = when {
            probability < 0 -> 0f
            isScam -> ((probability - threshold) / (1f - threshold)).coerceAtMost(1f)
            else -> ((threshold - probability) / threshold).coerceAtMost(1f)
        }

        val verdict = when {
            probability < 0 -> "safe"
            confidence < suspiciousConfidence -> "suspicious"
            isScam -> "scam"
            else -> "safe"
        }

        // 6. Map features to key findings (only when not safe)
        val keyFindings = if (verdict != "safe") {
            mapKeyFindings(featureArray)
        } else {
            emptyList()
        }

        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        return Gate1Result(
            verdict = verdict,
            riskScore = confidence,
            features = featureArray,
            keyFindings = keyFindings,
            responseTimeMs = elapsed
        )
    }

    /**
     * Run model inference.
     * TODO: Replace this placeholder with actual ONNX Runtime or TFLite call.
     */
    private fun runInference(features: FloatArray, urlIds: IntArray): Float {
        // Placeholder — replace with:
        //
        // ONNX Runtime:
        //   val featureTensor = OnnxTensor.createTensor(env, arrayOf(features))
        //   val urlTensor = OnnxTensor.createTensor(env, arrayOf(urlIds))
        //   val results = session.run(mapOf("features" to featureTensor, "url_ids" to urlTensor))
        //   val logit = (results[0].value as Array<FloatArray>)[0][0]
        //   return 1f / (1f + exp(-logit))
        //
        // TFLite:
        //   interpreter.run(inputArray, outputArray)
        //   val logit = outputArray[0][0]
        //   return 1f / (1f + exp(-logit))

        return -1f // Safe fallback until model is integrated
    }

    // MARK: - KeyFinding Mapping

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
            // One finding per category — first matching rule wins
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

    // MARK: - Brand Data

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

    fun close() {
        if (brandsPtr != 0L) {
            nativeFreeBrands(brandsPtr)
            brandsPtr = 0L
        }
        // session.close()
        // env.close()
    }
}
