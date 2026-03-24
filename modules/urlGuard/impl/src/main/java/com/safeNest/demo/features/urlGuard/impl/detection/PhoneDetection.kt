package com.safeNest.demo.features.urlGuard.impl.detection

import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus

interface PhoneDetection {
    suspend fun detectPhone(phone: String): DetectionStatus

    suspend fun getCallerInfo(phone: String): CallerIdInfo?
}
