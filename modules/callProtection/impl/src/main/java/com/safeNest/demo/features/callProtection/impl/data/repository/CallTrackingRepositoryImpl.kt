package com.safeNest.demo.features.callProtection.impl.data.repository

import com.safeNest.demo.features.callProtection.impl.data.local.CallTrackingDao
import com.safeNest.demo.features.callProtection.impl.domain.common.getCurrentDateString
import com.safeNest.demo.features.callProtection.impl.domain.repository.CallTrackingRepository
import javax.inject.Inject

class CallTrackingRepositoryImpl @Inject constructor(
    private val callTrackingDao: CallTrackingDao
) : CallTrackingRepository {
    override suspend fun add(phoneNumber: String) {
        callTrackingDao.trackIncomingCall(phoneNumber, getCurrentDateString())
    }

    override suspend fun get(phoneNumber: String) = callTrackingDao.getCallCountToday(phoneNumber, getCurrentDateString())?.toCallTracking()
}