package com.safeNest.demo.features.callProtection.impl.presentation.service.handler

interface CallDetectionHandler {
    suspend fun onCallRing(phoneNumber: String, isIncoming: Boolean = true): CallResult
    fun onCallAnswer(phoneNumber: String)
    suspend fun onCallEnd(phoneNumber: String)
}

sealed interface CallResult {
    object Reject : CallResult
    data class Allow(val isSilent: Boolean = false) : CallResult
}