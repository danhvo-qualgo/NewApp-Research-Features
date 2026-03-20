package com.safeNest.demo.features.urlGuard.impl.detection.model

sealed interface ModelDetectStatus {
    object Safe: ModelDetectStatus
    object Scam: ModelDetectStatus
    object Unknown: ModelDetectStatus

    object Warning: ModelDetectStatus
}