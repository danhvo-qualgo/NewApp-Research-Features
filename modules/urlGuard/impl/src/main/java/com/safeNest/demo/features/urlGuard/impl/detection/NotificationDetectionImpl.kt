package com.safeNest.demo.features.urlGuard.impl.detection

import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import com.safeNest.demo.features.urlGuard.impl.detection.mapper.toDetectionStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus
import javax.inject.Inject

class NotificationDetectionImpl @Inject constructor(
    private val analyzeUseCase: AnalyzeUseCase
) : NotificationDetection {
    override suspend fun detectNotificationContent(text: String): DetectionStatus {
        val analyzerResult = analyzeUseCase(AnalysisInput.Text("", "", text))
        Log.d(TAG, "NotificationDetection result: $analyzerResult")
        return analyzerResult?.status?.toDetectionStatus() ?: DetectionStatus.UNKNOWN
    }

    companion object {
        const val TAG = "NotificationDetection"
    }
}