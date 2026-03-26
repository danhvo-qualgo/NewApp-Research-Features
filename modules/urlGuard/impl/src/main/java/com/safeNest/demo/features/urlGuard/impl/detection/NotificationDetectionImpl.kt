package com.safeNest.demo.features.urlGuard.impl.detection

import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeAndGetResultUseCase
import com.safeNest.demo.features.urlGuard.impl.detection.mapper.toDetectionStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.DetectionStatus
import javax.inject.Inject

class NotificationDetectionImpl @Inject constructor(
    private val analyzeAndGetResultUseCase: AnalyzeAndGetResultUseCase
) : NotificationDetection {
    override suspend fun detectNotificationContent(text: String): DetectionStatus {
        val result = analyzeAndGetResultUseCase(AnalysisInput.Text("", "", text))
        Log.d(TAG, "NotificationDetection result: $result")
        
        return result.fold(
            onSuccess = { analysisResult ->
                analysisResult.status.toDetectionStatus()
            },
            onFailure = {
                DetectionStatus.UNKNOWN
            }
        )
    }

    companion object {
        const val TAG = "NotificationDetection"
    }
}