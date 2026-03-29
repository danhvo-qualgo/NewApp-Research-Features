package com.safeNest.demo.features.urlGuard.impl.detection

import android.util.Log
import com.safeNest.demo.features.urlGuard.impl.detection.model.ModelDetectStatus
import com.safenest.urlanalyzer.url.gate1.Gate1Classifier
import jakarta.inject.Inject

class UrlDetectionImpl @Inject constructor(
    private val classier1: Gate1Classifier
): UrlDetection {


    override fun detect(url: String): ModelDetectStatus {
        val response = classier1.classify(url)
        Log.d(TAG, "response for url $url: $response")
        return when(response.verdict) {
            "scam" -> ModelDetectStatus.Scam(reason = response.keyFindings.toReason())
            "suspicious" -> ModelDetectStatus.Warning(reason = response.keyFindings.toReason())
            else -> ModelDetectStatus.Safe
        }
    }

    override fun onDestroy() {
        classier1.release()
    }

    companion object {
        const val TAG = "UrlDetection"
    }
}

private fun List<com.safenest.urlanalyzer.url.gate1.Gate1KeyFinding>.toReason(): String {
    if (isEmpty()) return ""
    return asSequence()
        .map { it.description.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
        .joinToString(separator = "\n") { "• $it" }
}
