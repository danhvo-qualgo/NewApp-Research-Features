package com.safeNest.demo.features.callProtection.impl.data.local

import kotlinx.coroutines.flow.Flow

interface CallDeviceStore {
    suspend fun setEnableWhitelist(isEnable: Boolean)
    fun isEnableWhitelist(): Flow<Boolean>
    suspend fun setEnableBlacklist(isEnable: Boolean)
    fun isEnableBlacklist(): Flow<Boolean>
}