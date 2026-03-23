package com.safeNest.demo.features.urlGuard.impl.detection.mapper

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus

fun AnalysisStatus?.toDetectionStatus(): DetectionStatus {
    return when(this) {
        AnalysisStatus.Scam -> DetectionStatus.DANGEROUS
        AnalysisStatus.Safe -> DetectionStatus.SAFE
        AnalysisStatus.Unverified -> DetectionStatus.WARNING
        else -> DetectionStatus.UNKNOWN
    }
}