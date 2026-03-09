package com.safeNest.features.call.callDetection.impl.presentation.service.handler

import android.util.Log
import com.safeNest.features.call.callDetection.impl.domain.usecase.GetBlacklistPatternsUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.GetPhoneNumberUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallDetectionHandlerImpl @Inject constructor(
    private val getPhoneNumberUseCase: GetPhoneNumberUseCase,
    private val getBlacklistPatternsUseCase: GetBlacklistPatternsUseCase
) : CallDetectionHandler {

    override suspend fun onCallRing(phoneNumber: String): CallResult {
        return getPhoneNumberUseCase(phoneNumber).first()?.takeIf {
            getBlacklistPatternsUseCase().first().also {
                Log.d("CallDetectionHandler", "onCallRing:  phoneNumber: $phoneNumber blacklist: $it")
            }.all { pattern ->
                !phoneNumber.contains(pattern.pattern)
            }
        }?.let {
            CallResult.Allow()
        } ?: CallResult.Reject
    }

    override fun onCallAnswer() {
    }

    override fun onCallEnd() {
    }
}