package com.safeNest.demo.features.urlGuard.impl.detection

import android.util.Log
import com.safeNest.demo.features.urlGuard.impl.detection.model.ModelDetectStatus
import com.safenest.urlanalyzer.gate1.Gate1Classifier
import jakarta.inject.Inject

class UrlDetectionImpl @Inject constructor(
    private val classier1: Gate1Classifier
): UrlDetection {


    override fun detect(url: String): ModelDetectStatus {
        val response = classier1.classify(url)
        Log.d(TAG, "response for url $url: $response")
        return when(response.data.verdict) {
            "scam" -> ModelDetectStatus.Scam
            "suspicious" -> ModelDetectStatus.Unknown
            else -> ModelDetectStatus.Safe
        }
    }

    override fun onDestroy() {
        classier1.close()
    }

    companion object {
        const val TAG = "UrlDetection"
    }
}