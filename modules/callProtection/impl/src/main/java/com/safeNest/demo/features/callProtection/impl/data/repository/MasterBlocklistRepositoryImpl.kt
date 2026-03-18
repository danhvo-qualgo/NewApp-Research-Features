package com.safeNest.demo.features.callProtection.impl.data.repository

import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStore
import com.safeNest.demo.features.callProtection.impl.data.local.MasterBlocklistDao
import com.safeNest.demo.features.callProtection.impl.domain.model.PhoneNumberInfo
import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterBlocklistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MasterBlocklistRepositoryImpl @Inject constructor(
    private val dao: MasterBlocklistDao,
    private val store: CallDeviceStore
) : MasterBlocklistRepository {

    override fun getBlocklist(): Flow<List<PhoneNumberInfo>> =
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
        dao.insert(number.toMasterBlocklistEntity())
    }

    override suspend fun remove(number: String) {
        dao.delete(number)
    }

    override suspend fun isBlocklisted(number: String): Boolean {
        return dao.exists(number)
    }
}