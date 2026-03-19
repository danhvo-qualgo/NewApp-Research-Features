/*
 * Gate1Classifier.kt — SafeNest Gate 1 URL Safety Classifier for Android
 *
 * Uses ONNX Runtime for model inference and JNI for C feature extraction.
 * Add to your project:
 *   1. gate1_late_fusion.onnx in assets/
 *   2. brands.bin in assets/
 *   3. libgate1_features.so (prebuilt per ABI, or build via CMake)
 */

package com.safenest.gate1

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.nio.FloatBuffer
import java.nio.LongBuffer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.exp
import kotlin.properties.Delegates

/**
 * Response types — unified with Gate 2 Backend API.
 */
data class Gate1KeyFinding(
    val category: String,
    val description: String,
    val severity: String
)

data class Gate1ResponseData(
    val riskScore: Float,           // confidence (0.0–1.0), threshold-relative
    val verdict: String,            // "safe", "suspicious", "scam"
    val keyFindings: List<Gate1KeyFinding>
)

data class Gate1Response(
    val data: Gate1ResponseData,
    val responseTime: String,       // e.g. "4ms"
    val timestamp: Long             // Unix epoch milliseconds
)

/**
 * Gate 1 URL Safety Classifier for Android.
 *
 * Usage:
 * ```kotlin
 * val classifier = Gate1Classifier(context)
 * val response = classifier.classify("https://example.com")
 * if (response.data.verdict == "scam") { /* block or warn */ }
 * classifier.close()
 * ```
 */
@OptIn(ExperimentalAtomicApi::class)
class Gate1Classifier(
    private val context: Context,
    private val threshold: Float = 0.2f,
    private val suspiciousConfidence: Float = 0.5f
) : AutoCloseable {

    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private lateinit var session: OrtSession
    private var brandsPtr by Delegates.notNull<Long>()  // Native pointer to Gate1BrandList

    private var isInitialized: AtomicBoolean = AtomicBoolean(false)

    init {
//        // Load ONNX model from assets
//        val modelBytes = context.assets.open("gate1_late_fusion.onnx").readBytes()
//        session = ortEnv.createSession(modelBytes)
//
//        // Load brands.bin from assets via native code
//        val brandsBytes = context.assets.open("brands.bin").readBytes()
//        brandsPtr = nativeLoadBrands(brandsBytes)
//        require(brandsPtr != 0L) { "Failed to load brands.bin" }
//        isInitialized.exchange(true)
        loadModel()
    }

    fun loadModel() {
        if (!isInitialized.load()) {
            // Load ONNX model from assets
            val modelBytes = context.assets.open("gate1_late_fusion.onnx").readBytes()
            session = ortEnv.createSession(modelBytes)

            // Load brands.bin from assets via native code
            val brandsBytes = context.assets.open("brands.bin").readBytes()
            brandsPtr = nativeLoadBrands(brandsBytes)
            require(brandsPtr != 0L) { "Failed to load brands.bin" }
            isInitialized.exchange(true)
        }
    }


    /**
     * Classify a URL. Returns a unified response matching Gate 2 Backend API format.
     */
    fun classify(url: String): Gate1Response {
        val startTime = System.nanoTime()

        // 1. Allowlist gate (native)
        if (nativeIsAllowlisted(url, brandsPtr)) {
            val elapsed = (System.nanoTime() - startTime) / 1_000_000
            return Gate1Response(
                data = Gate1ResponseData(riskScore = 1.0f, verdict = "safe", keyFindings = emptyList()),
                responseTime = "${elapsed}ms",
                timestamp = System.currentTimeMillis()
            )
        }

        // 2. Extract features (native -> 30 floats)
        val features = nativeExtractFeatures(url, brandsPtr)

        // 3. Encode URL (native -> 200 ints as longs for ONNX)
        val encoding = nativeEncodeUrl(url)

        // 4. ONNX Runtime inference
        val urlTensor = OnnxTensor.createTensor(
            ortEnv, LongBuffer.wrap(encoding), longArrayOf(1, 200)
        )
        val featTensor = OnnxTensor.createTensor(
            ortEnv, FloatBuffer.wrap(features), longArrayOf(1, 30)
        )

        val results = session.run(
            mapOf("url_ids" to urlTensor, "features" to featTensor)
        )

        val logit = (results[0].value as FloatArray)[0]
        val probability = sigmoid(logit)
        val isScam = probability >= threshold
        val confidence = if (isScam) {
            ((probability - threshold) / (1.0f - threshold)).coerceIn(0f, 1f)
        } else {
            ((threshold - probability) / threshold).coerceIn(0f, 1f)
        }

        urlTensor.close()
        featTensor.close()
        results.close()

        // 5. Determine verdict
        val verdict = when {
            confidence < suspiciousConfidence -> "suspicious"
            isScam -> "scam"
            else -> "safe"
        }

        val elapsed = (System.nanoTime() - startTime) / 1_000_000
        return Gate1Response(
            data = Gate1ResponseData(riskScore = confidence, verdict = verdict, keyFindings = emptyList()),
            responseTime = "${elapsed}ms",
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Batch classify multiple URLs.
     */
    fun classify(urls: List<String>): List<Gate1Response> = urls.map { classify(it) }

    override fun close() {
        nativeFreeBrands(brandsPtr)
        session.close()
    }

    private fun sigmoid(x: Float): Float = 1.0f / (1.0f + exp(-x))

    // ── JNI Native Methods ──

    private external fun nativeLoadBrands(data: ByteArray): Long
    private external fun nativeFreeBrands(ptr: Long)
    private external fun nativeIsAllowlisted(url: String, brandsPtr: Long): Boolean
    private external fun nativeExtractFeatures(url: String, brandsPtr: Long): FloatArray
    private external fun nativeEncodeUrl(url: String): LongArray

    companion object {
        init {
            System.loadLibrary("gate1_features")
        }
    }
}
