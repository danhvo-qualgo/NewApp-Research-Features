package com.safeNest.demo.features.callProtection.impl.data.repository

import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.impl.domain.repository.CallDetectionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CallDetectionRepositoryImpl @Inject constructor() : CallDetectionRepository {
    private val callerIdInfos: List<CallerIdInfo> = listOf(
        CallerIdInfo("+84364384264", "Scam - Open KinShield for Detail", CallerIdInfoType.SCAM),
        CallerIdInfo("+84903468025", "Spam - Open KinShield for Detail", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491861", "John Vo Scam", CallerIdInfoType.SCAM),
        CallerIdInfo("+84902895081", "Spam - Open KinShield for Detail", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491868", "John Vo Spam", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491867", "Spam - Open KinShield for Detail", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491881", "John Vo Spam", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491882", "John Vo Spam", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491883", "John Vo Spam", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491884", "John Vo Spam", CallerIdInfoType.SPAM),
        CallerIdInfo("+84393491863", "John Phishing ", CallerIdInfoType.PHISHING),
        CallerIdInfo("+84393491864", "John Unknow ", CallerIdInfoType.UNKNOW),
        CallerIdInfo("+84393491865", "John Safe ", CallerIdInfoType.SAFE),
        CallerIdInfo("+84352971661", "Trang", CallerIdInfoType.SAFE),
        CallerIdInfo("+84352971620", "Trang", CallerIdInfoType.SAFE),
    )

    override fun evaluatePhoneNumber(phoneNumber: String): Flow<CallerIdInfo?> = flowOf(
        callerIdInfos.firstOrNull { it.phoneNumber == phoneNumber }
    )
}