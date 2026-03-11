package com.safeNest.features.call.callDetection.impl.data.local

import com.uney.core.storage.api.DeviceStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CallDeviceStoreImpl @Inject constructor(
    private val deviceStorage: DeviceStorage
): CallDeviceStore {
    override suspend fun setEnableWhitelist(isEnable: Boolean) {
        deviceStorage.setBoolean(ENABLE_WHITELIST, isEnable)
    }

    override fun isEnableWhitelist(): Flow<Boolean> {
       return deviceStorage.getBoolean(ENABLE_WHITELIST).map { it ?: false }

    }

    override suspend fun setEnableBlacklist(isEnable: Boolean) {
        deviceStorage.setBoolean(ENABLE_BLACKLIST, isEnable)
    }

    override fun isEnableBlacklist(): Flow<Boolean> {
        return deviceStorage.getBoolean(ENABLE_BLACKLIST).map { it ?: false }
    }

    companion object {
        const val ENABLE_WHITELIST = "ENABLE_WHITELIST"
        const val ENABLE_BLACKLIST = "ENABLE_BLACKLIST"

    }
}