package com.safeNest.demo.features.callProtection.impl.data.repository

import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStore
import com.safeNest.demo.features.callProtection.impl.data.local.MasterWhitelistDao
import com.safeNest.demo.features.callProtection.impl.domain.model.PhoneNumberInfo
import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterWhitelistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MasterWhitelistRepositoryImpl @Inject constructor(
    private val dao: MasterWhitelistDao,
    private val store: CallDeviceStore
) : MasterWhitelistRepository {

    override fun getWhitelist(): Flow<List<PhoneNumberInfo>> =
        dao.getAll().map { list ->
            list.map { it.toPhoneNumberInfo() }
        }

    override fun getPhoneNumber(phoneNumber: String): Flow<PhoneNumberInfo?> =
        dao.get(phoneNumber).map { it?.toPhoneNumberInfo() }

    override fun isEnable(): Flow<Boolean> {
        return store.isEnableWhitelist()
    }

    override suspend fun setEnable(isEnable: Boolean) {
        store.setEnableWhitelist(isEnable)
    }

    override suspend fun add(number: PhoneNumberInfo) {
        dao.insert(number.toMasterWhitelistEntity())
    }

    override suspend fun remove(number: String) {
        dao.delete(number)
    }

    override suspend fun isWhitelisted(number: String): Boolean {
        return dao.exists(number)
    }
}