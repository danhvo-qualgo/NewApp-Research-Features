package com.safeNest.demo.features.callProtection.impl.presentation.service.handler

import android.util.Log
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlackListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableWhiteListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetBlacklistPatternsUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetPhoneNumberUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallDetectionHandlerImpl @Inject constructor(
    private val getPhoneNumberUseCase: GetPhoneNumberUseCase,
    private val getBlacklistPatternsUseCase: GetBlacklistPatternsUseCase,
    private val enableBlackListUseCase: EnableBlackListUseCase,
    private val enableWhiteListUseCase: EnableWhiteListUseCase
) : CallDetectionHandler {

    override suspend fun onCallRing(phoneNumber: String): CallResult {
        val isEnableWhitelist = enableWhiteListUseCase.isEnable().first()
        val isEnableBlacklist = enableBlackListUseCase.isEnable().first()
        if (!isEnableWhitelist && !isEnableBlacklist) {
            return CallResult.Allow()
        }

        if (isEnableWhitelist && !isEnableBlacklist) {
            return getPhoneNumberUseCase(phoneNumber).first()?.let {
                CallResult.Allow()
            } ?: CallResult.Reject
        }

        if (!isEnableWhitelist) {
            return getBlacklistPatternsUseCase().first().also {
                Log.d(
                    "CallDetectionHandler",
                    "onCallRing:  phoneNumber: $phoneNumber blacklist: $it"
                )
            }.all { pattern ->
                !phoneNumber.startsWith(pattern.pattern)
            }.takeIf { it }?.let {
                return CallResult.Allow()
            } ?: CallResult.Reject
        }
        return getPhoneNumberUseCase(phoneNumber).first()?.takeIf {
            getBlacklistPatternsUseCase().first().also {
                Log.d(
                    "CallDetectionHandler",
                    "onCallRing:  phoneNumber: $phoneNumber blacklist: $it"
                )
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