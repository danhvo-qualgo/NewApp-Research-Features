/*
 * Gate1Classifier.kt — URL classification via ONNX Runtime LightGBM + brands allowlist.
 * Uses gate1_features.c via JNI for feature extraction.
 */
package com.safenest.urlanalyzer.url.gate1

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import org.json.JSONArray
import org.json.JSONObject

data class Gate1Result(
    val url: String,
    val verdict: String,
    val riskScore: Double,
    val keyFindings: List<Gate1KeyFinding>
)

data class Gate1KeyFinding(
    val category: String,
    val description: String
)

class Gate1Classifier(context: Context) {

    companion object {
        private const val TAG = "Gate1"
        private const val THRESHOLD = 0.2
        private const val SUSPICIOUS_CONFIDENCE = 0.5

        init {
            System.loadLibrary("safenest_jni")
        }
    }

    private val ortEnv = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val brandsPtr: Long
    private val keyfindingMappings: JSONArray

    // JNI — matches gate1_jni.c
    private external fun nativeLoadBrands(data: ByteArray): Long
    private external fun nativeFreeBrands(ptr: Long)
    private external fun nativeIsAllowlisted(url: String, brandsPtr: Long): Boolean
    private external fun nativeExtractFeatures(url: String, brandsPtr: Long): FloatArray?

    init {
        // Load ONNX model
        val modelBytes = context.assets.open("gate1_lightgbm.onnx").readBytes()
        session = ortEnv.createSession(modelBytes)
        Log.i(TAG, "ONNX model loaded")

        // Load brands via JNI
        val brandsData = context.assets.open("brands.bin").readBytes()
        brandsPtr = nativeLoadBrands(brandsData)
        Log.i(TAG, "Brands loaded: ptr=$brandsPtr")

        // Load keyfinding mappings — file is {"mappings": [...]}
        val mappingsJson = context.assets.open("config/gate1_keyfinding_mappings.json")
            .bufferedReader().use { it.readText() }
        val mappingsObj = JSONObject(mappingsJson)
        keyfindingMappings = mappingsObj.getJSONArray("mappings")
        Log.i(TAG, "Keyfinding mappings loaded: ${keyfindingMappings.length()} rules")
    }

    fun classify(url: String): Gate1Result {
        // Step 1: Allowlist check via native code
        if (brandsPtr != 0L && nativeIsAllowlisted(url, brandsPtr)) {
            return Gate1Result(url, "safe", 0.0, emptyList())
        }

        // Step 2: Feature extraction via JNI
        val features = nativeExtractFeatures(url, brandsPtr)
            ?: return Gate1Result(url, "unknown", 0.5, emptyList())

        // Step 3: ONNX inference
        val tensor = OnnxTensor.createTensor(ortEnv, arrayOf(features))
        val results = session.run(mapOf("features" to tensor))

        // ONNX Runtime may return probabilities as different types depending on model
        val scamProb: Double = try {
            // Try Array<FloatArray> first (common for LightGBM ONNX)
            @Suppress("UNCHECKED_CAST")
            val probs = results[1].value as Array<FloatArray>
            probs[0][1].toDouble()
        } catch (e: ClassCastException) {
            try {
                // Try List<Map<Long, Float>> (LightGBM ZipMap output)
                @Suppress("UNCHECKED_CAST")
                val probList = results[1].value as List<Map<Long, Float>>
                probList[0][1L]?.toDouble() ?: 0.5
            } catch (e2: ClassCastException) {
                Log.e(TAG, "ONNX output type: ${results[1].value?.javaClass}", e2)
                0.5
            }
        }
        tensor.close()

        // Step 4: Threshold logic
        val isScam = scamProb >= THRESHOLD
        val confidence = if (isScam) {
            ((scamProb - THRESHOLD) / (1.0 - THRESHOLD)).coerceAtMost(1.0)
        } else {
            ((THRESHOLD - scamProb) / THRESHOLD).coerceAtMost(1.0)
        }

        val verdict = when {
            confidence < SUSPICIOUS_CONFIDENCE -> "suspicious"
            isScam -> "scam"
            else -> "safe"
        }

        // Step 5: Key findings
        val keyFindings = extractKeyFindings(features)

        return Gate1Result(url, verdict, scamProb, keyFindings)
    }

    private fun extractKeyFindings(features: FloatArray): List<Gate1KeyFinding> {
        val findings = mutableListOf<Gate1KeyFinding>()
        val seenCategories = mutableSetOf<String>()

        for (i in 0 until keyfindingMappings.length()) {
            val mapping = keyfindingMappings.getJSONObject(i)
            val featureIdx = mapping.getInt("featureIndex")
            val condition = mapping.getString("condition") // e.g. ">= 0.7", "== 1", "> 0"
            val category = mapping.getString("category")
            val description = mapping.getString("description")

            if (featureIdx >= features.size) continue
            if (category in seenCategories) continue

            // Parse condition: "op value"
            val parts = condition.trim().split(" ", limit = 2)
            if (parts.size != 2) continue
            val op = parts[0]
            val threshold = parts[1].toDoubleOrNull() ?: continue

            val value = features[featureIdx].toDouble()
            val triggered = when (op) {
                ">" -> value > threshold
                ">=" -> value >= threshold
                "<" -> value < threshold
                "<=" -> value <= threshold
                "==" -> value == threshold
                else -> false
            }

            if (triggered) {
                findings.add(Gate1KeyFinding(category, description))
                seenCategories.add(category)
            }
        }

        return findings
    }

    fun release() {
        if (brandsPtr != 0L) {
            nativeFreeBrands(brandsPtr)
        }
    }
}
