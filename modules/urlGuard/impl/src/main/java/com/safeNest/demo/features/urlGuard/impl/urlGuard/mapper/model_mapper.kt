package com.safeNest.demo.features.urlGuard.impl.urlGuard.mapper

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import com.safeNest.demo.features.urlGuard.impl.detection.model.ModelDetectStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.DetectionStatus

fun ModelDetectStatus.toModelDetectionStatus(): DetectionStatus {
    return when(this) {
        ModelDetectStatus.Safe -> DetectionStatus.SAFE
        ModelDetectStatus.Scam -> DetectionStatus.DANGEROUS
        ModelDetectStatus.Warning -> DetectionStatus.WARNING
        ModelDetectStatus.Unknown -> DetectionStatus.UNKNOWN
    }
}

fun AnalyzeMode.toBlockingText(): String = when (this) {
    AnalyzeMode.Local -> "On-device"
    AnalyzeMode.Remote -> "Remote"
}