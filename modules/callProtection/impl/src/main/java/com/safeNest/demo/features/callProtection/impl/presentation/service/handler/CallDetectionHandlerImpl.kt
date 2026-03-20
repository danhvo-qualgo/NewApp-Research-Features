package com.safeNest.demo.features.callProtection.impl.presentation.service.handler

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.api.domain.model.GetCallerIdInfoUseCase
import com.safeNest.demo.features.callProtection.impl.domain.common.normalizePhoneNumber
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddCallTrackingUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlockListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableWhiteListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetCallTrackingUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetMasterBlocklistNumberUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetWhitelistByNumberUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.IsBlocklistPatternsUseCase
import com.safeNest.demo.features.callProtection.impl.presentation.service.call.CallDetectionPopup
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallDetectionHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getWhitelistByNumberUseCase: GetWhitelistByNumberUseCase,
    private val isBlocklistPatternsUseCase: IsBlocklistPatternsUseCase,
    private val enableBlockListUseCase: EnableBlockListUseCase,
    private val enableWhiteListUseCase: EnableWhiteListUseCase,
    private val getMasterBlocklistNumberUseCase: GetMasterBlocklistNumberUseCase,
    private val getMasterWhitelistNumberUseCase: GetMasterBlocklistNumberUseCase,
    private val getCallerIdUseCase: GetCallerIdInfoUseCase,
    private val addCallTrackingUseCase: AddCallTrackingUseCase,
) : CallDetectionHandler {

    override suspend fun onCallRing(phoneNumber: String): CallResult {
        val normalizePhoneNumber = normalizePhoneNumber(phoneNumber)
        Log.v("CallDetectionHandlerImpl", "normalizePhoneNumber: $normalizePhoneNumber")
        if (getMasterWhitelistNumberUseCase(normalizePhoneNumber).first() != null) {
            return CallResult.Allow()
        }
        if (getMasterBlocklistNumberUseCase(normalizePhoneNumber).first() != null || getMasterBlocklistNumberUseCase(phoneNumber).first() != null) {
            return CallResult.Reject
        }
        Log.v("CallDetectionHandlerImpl", "start check whitelist: $normalizePhoneNumber")

        val isEnableWhitelist = enableWhiteListUseCase.isEnable().first()
        val isEnableBlacklist = enableBlockListUseCase.isEnable().first()
        addCallTrackingUseCase(normalizePhoneNumber)
        if (isEnableWhitelist) {
            return getWhitelistByNumberUseCase(normalizePhoneNumber).first()?.let {
                CallResult.Allow()
            } ?: CallResult.Reject
        }
        Log.v("CallDetectionHandlerImpl", "start check blocklist: $normalizePhoneNumber")

        if (isEnableBlacklist && isBlocklistPatternsUseCase(normalizePhoneNumber).first()) {
            return CallResult.Reject
        }
        Log.v("CallDetectionHandlerImpl", "onCallRing: $normalizePhoneNumber")
        getCallerIdUseCase(normalizePhoneNumber)?.let {
            Log.v("CallDetectionHandlerImpl", "onCallRing: $it")
            return when(it.type) {
                CallerIdInfoType.SCAM -> {
                    CallResult.Reject
                }
                else -> {
                    Handler(Looper.getMainLooper()).post {
                        CallDetectionPopup.show(context, CallDetectionPopup.PopupContent(normalizePhoneNumber, it.type))
                    }
                    CallResult.Allow()
                }
            }
        }

        return CallResult.Allow()
    }

    override fun onCallAnswer(phoneNumber: String) {
        CallDetectionPopup.dismiss(context)
    }

    override suspend fun onCallEnd(phoneNumber: String) {
        CallDetectionPopup.dismiss(context)
    }
}