package com.safeNest.demo.features.urlGuard.impl.detection.model

sealed interface ModelDetectStatus {
    val reason: String

    object Safe : ModelDetectStatus {
        override val reason: String = ""
    }

    data class Scam(
        override val reason: String = ""
    ) : ModelDetectStatus

    object Unknown : ModelDetectStatus {
        override val reason: String = ""
    }

    data class Warning(
        override val reason: String = ""
    ) : ModelDetectStatus
}
