package com.safeNest.demo.features.callProtection.impl.domain.repository

import com.safeNest.demo.features.callProtection.impl.domain.model.CallTracking

interface CallTrackingRepository {
    suspend fun add(phoneNumber: String)
    suspend fun get(phoneNumber: String): CallTracking?
}