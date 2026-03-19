package com.safeNest.demo.features.callProtection.impl.data.repository

import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.impl.domain.repository.CallDetectionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CallDetectionRepositoryImpl @Inject constructor() : CallDetectionRepository {
    private val callerIdInfos: List<CallerIdInfo> = listOf(
        CallerIdInfo("+84393491867", "John Vo Scam", CallerIdInfoType.SCAM),
        CallerIdInfo("+84393491866", "John Vo Spam", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491865", "John Phishing ", CallerIdInfoType.PHISHING),
    )

    override fun evaluatePhoneNumber(phoneNumber: String): Flow<CallerIdInfo?> = flowOf(
        callerIdInfos.firstOrNull { it.phoneNumber == phoneNumber }
    )
}