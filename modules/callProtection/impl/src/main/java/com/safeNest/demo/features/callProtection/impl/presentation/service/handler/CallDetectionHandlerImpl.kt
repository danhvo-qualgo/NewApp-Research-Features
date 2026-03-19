package com.safeNest.demo.features.callProtection.impl.presentation.service.handler

import com.safeNest.demo.features.callProtection.impl.domain.common.normalizePhoneNumber
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlockListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableWhiteListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetMasterBlocklistNumberUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetWhitelistByNumberUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.IsBlocklistPatternsUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallDetectionHandlerImpl @Inject constructor(
    private val getWhitelistByNumberUseCase: GetWhitelistByNumberUseCase,
    private val isBlocklistPatternsUseCase: IsBlocklistPatternsUseCase,
    private val enableBlockListUseCase: EnableBlockListUseCase,
    private val enableWhiteListUseCase: EnableWhiteListUseCase,
    private val getMasterBlocklistNumberUseCase: GetMasterBlocklistNumberUseCase,
    private val getMasterWhitelistNumberUseCase: GetMasterBlocklistNumberUseCase
) : CallDetectionHandler {

    override suspend fun onCallRing(phoneNumber: String): CallResult {
        val normalizePhoneNumber = normalizePhoneNumber(phoneNumber)
        if (getMasterWhitelistNumberUseCase(normalizePhoneNumber).first() != null) {
            return CallResult.Allow()
        }
        if (getMasterBlocklistNumberUseCase(normalizePhoneNumber).first() != null) {
            return CallResult.Reject
        }

        val isEnableWhitelist = enableWhiteListUseCase.isEnable().first()
        val isEnableBlacklist = enableBlockListUseCase.isEnable().first()

        if (isEnableWhitelist) {
            return getWhitelistByNumberUseCase(phoneNumber).first()?.let {
                CallResult.Allow()
            } ?: CallResult.Reject
        }

        if (isEnableBlacklist && isBlocklistPatternsUseCase(normalizePhoneNumber).first()) {
            return CallResult.Reject
        }

        return CallResult.Allow()
    }

    override fun onCallAnswer() {
    }

    override fun onCallEnd() {
    }
}