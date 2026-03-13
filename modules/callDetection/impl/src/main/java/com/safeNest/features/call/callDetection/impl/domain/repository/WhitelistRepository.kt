package com.safeNest.features.call.callDetection.impl.domain.repository

import com.safeNest.features.call.callDetection.impl.domain.model.WhitelistNumber
import kotlinx.coroutines.flow.Flow

interface WhitelistRepository {
    fun getWhitelist(): Flow<List<WhitelistNumber>>
    suspend fun add(number: WhitelistNumber)
    suspend fun remove(number: String)
    suspend fun isWhitelisted(number: String): Boolean
    fun getPhoneNumber(phoneNumber: String): Flow<WhitelistNumber?>
    fun isEnable(): Flow<Boolean>
    suspend fun setEnable(isEnable: Boolean)
}