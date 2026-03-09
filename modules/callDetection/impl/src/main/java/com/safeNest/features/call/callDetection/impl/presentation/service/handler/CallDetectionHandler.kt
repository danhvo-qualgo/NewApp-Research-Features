package com.safeNest.features.call.callDetection.impl.presentation.service.handler

interface CallDetectionHandler {
    suspend fun onCallRing(phoneNumber: String): CallResult
    fun onCallAnswer()
    fun onCallEnd()
}

sealed interface CallResult {
    object Reject : CallResult
    data class Allow(val isSilent: Boolean = false) : CallResult
}