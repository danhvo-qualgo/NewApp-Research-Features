package com.safeNest.demo.features.urlGuard.impl.urlGuard.mapper

import com.safeNest.demo.features.urlGuard.impl.detection.model.ModelDetectStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus

fun ModelDetectStatus.toModelDetectionStatus(): DetectionStatus {
    return when(this) {
        ModelDetectStatus.Safe -> DetectionStatus.SAFE
        ModelDetectStatus.Scam -> DetectionStatus.DANGEROUS
        ModelDetectStatus.Unknown -> DetectionStatus.UNKNOWN
    }
}