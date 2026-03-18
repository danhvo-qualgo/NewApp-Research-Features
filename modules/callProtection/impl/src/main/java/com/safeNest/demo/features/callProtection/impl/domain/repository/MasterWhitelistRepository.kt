package com.safeNest.demo.features.callProtection.impl.domain.repository

import com.safeNest.demo.features.callProtection.impl.domain.model.PhoneNumberInfo
import kotlinx.coroutines.flow.Flow

interface MasterWhitelistRepository {
    fun getWhitelist(): Flow<List<PhoneNumberInfo>>
    suspend fun add(number: PhoneNumberInfo)
    suspend fun remove(number: String)
    suspend fun isWhitelisted(number: String): Boolean
    fun getPhoneNumber(phoneNumber: String): Flow<PhoneNumberInfo?>
    fun isEnable(): Flow<Boolean>
    suspend fun setEnable(isEnable: Boolean)
}