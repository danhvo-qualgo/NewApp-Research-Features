package com.safeNest.demo.features.urlGuard.impl.detection

import android.util.Log
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.api.domain.model.GetCallerIdInfoUseCase
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus
import jakarta.inject.Inject

val listDatabasePhone = listOf(
    CallerIdInfo("123456", "John Vo Scam", CallerIdInfoType.SCAM),
    CallerIdInfo("123457", "John Vo Scam", CallerIdInfoType.PHISHING),
    CallerIdInfo("123458", "John Vo Scam", CallerIdInfoType.SAFE),
    CallerIdInfo("123459", "John Vo Scam", CallerIdInfoType.SCAM),
)
class PhoneDetectionImpl @Inject constructor(
    private val getCallerIdInfoUseCase: GetCallerIdInfoUseCase
): PhoneDetection {
    override suspend fun detectPhone(phone: String): DetectionStatus {
        val getCallerInfo = getCallerIdInfoUseCase(phone) ?: runCatching {listDatabasePhone.first { phone == it.phoneNumber }}.getOrNull()
        Log.d(TAG, "getCallerInfo: $getCallerInfo")
        return when(getCallerInfo?.type) {
            CallerIdInfoType.PHISHING -> DetectionStatus.DANGEROUS
            CallerIdInfoType.SCAM, CallerIdInfoType.SPAM -> DetectionStatus.WARNING
            CallerIdInfoType.SAFE -> DetectionStatus.SAFE
            else -> DetectionStatus.WARNING
        }
    }

    override suspend fun getCallerInfo(phone: String): CallerIdInfo? {
        return getCallerIdInfoUseCase(phone)

    }
    companion object {
        const val TAG = "PhoneDetection"
    }
}