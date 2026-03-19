package com.safeNest.demo.features.callProtection.impl.domain.repository

import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import kotlinx.coroutines.flow.Flow

interface CallDetectionRepository {
    fun evaluatePhoneNumber(phoneNumber: String): Flow<CallerIdInfo?>
}