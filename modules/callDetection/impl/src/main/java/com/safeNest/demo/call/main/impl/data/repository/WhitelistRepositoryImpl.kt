package com.safeNest.demo.call.main.impl.data.repository

import com.safeNest.demo.call.main.impl.data.local.CallDeviceStore
import com.safeNest.demo.call.main.impl.data.local.WhitelistDao
import com.safeNest.demo.call.main.impl.data.local.WhitelistEntity
import com.safeNest.demo.call.main.impl.domain.model.WhitelistNumber
import com.safeNest.demo.call.main.impl.domain.repository.WhitelistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WhitelistRepositoryImpl @Inject constructor(
    private val dao: WhitelistDao,
    private val store: CallDeviceStore
) : WhitelistRepository {

    override fun getWhitelist(): Flow<List<WhitelistNumber>> =
        dao.getAll().map { list ->
            list.map { WhitelistNumber(it.phoneNumber) }
        }

    override fun getPhoneNumber(phoneNumber: String): Flow<WhitelistNumber?> =
        dao.get(phoneNumber).map { it?.let { it1 -> WhitelistNumber(it1.phoneNumber) } }

    override fun isEnable(): Flow<Boolean> {
        return store.isEnableWhitelist()
    }

    override suspend fun setEnable(isEnable: Boolean) {
        store.setEnableWhitelist(isEnable)
    }

    override suspend fun add(number: String) {
        dao.insert(WhitelistEntity(number))
    }

    override suspend fun remove(number: String) {
        dao.delete(number)
    }

    override suspend fun isWhitelisted(number: String): Boolean {
        return dao.exists(number)
    }
}