package com.safeNest.demo.features.callProtection.impl.data.repository

import android.util.Log
import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStore
import com.safeNest.demo.features.callProtection.impl.data.local.WhitelistDao
import com.safeNest.demo.features.callProtection.impl.domain.model.PhoneNumberInfo
import com.safeNest.demo.features.callProtection.impl.domain.repository.WhitelistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WhitelistRepositoryImpl @Inject constructor(
    private val dao: WhitelistDao,
    private val store: CallDeviceStore
) : WhitelistRepository {

    override fun getWhitelist(): Flow<List<PhoneNumberInfo>> =
        dao.getAll().map { list ->
            list.map { it.toPhoneNumberInfo() }
        }

    override fun getPhoneNumber(phoneNumber: String): Flow<PhoneNumberInfo?> =
        dao.get(phoneNumber).map {
            Log.v("WhitelistRepositoryImpl", "getPhoneNumber: $it")
            it?.toPhoneNumberInfo()
        }

    override fun isEnable(): Flow<Boolean> {
        return store.isEnableWhitelist()
    }

    override suspend fun setEnable(isEnable: Boolean) {
        store.setEnableWhitelist(isEnable)
    }

    override suspend fun add(number: PhoneNumberInfo) {
        dao.insert(number.toWhitelistEntity())
    }

    override suspend fun remove(number: String) {
        dao.delete(number)
    }

    override suspend fun isWhitelisted(number: String): Boolean {
        return dao.exists(number)
    }
}