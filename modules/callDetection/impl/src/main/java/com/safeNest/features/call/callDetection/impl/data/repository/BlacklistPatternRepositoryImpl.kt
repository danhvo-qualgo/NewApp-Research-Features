package com.safeNest.features.call.callDetection.impl.data.repository

import com.safeNest.features.call.callDetection.impl.data.local.BlacklistPatternDao
import com.safeNest.features.call.callDetection.impl.data.local.BlacklistPatternEntity
import com.safeNest.features.call.callDetection.impl.data.local.CallDeviceStore
import com.safeNest.features.call.callDetection.impl.domain.model.BlacklistPattern
import com.safeNest.features.call.callDetection.impl.domain.repository.BlacklistPatternRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlacklistPatternRepositoryImpl @Inject constructor(
    private val dao: BlacklistPatternDao,
    private val store: CallDeviceStore
) : BlacklistPatternRepository {

    override fun getBlacklistPatterns(): Flow<List<BlacklistPattern>>  =
        dao.getAll().map { list ->
            list.map { BlacklistPattern(it.pattern) }
        }
    override suspend fun add(pattern: String) {
        dao.insert(BlacklistPatternEntity(pattern))
    }

    override suspend fun remove(pattern: String) {
        dao.delete(pattern)
    }

    override fun isEnable(): Flow<Boolean>  {
        return store.isEnableBlacklist()
    }

    override suspend fun setEnable(isEnable: Boolean) {
        store.setEnableBlacklist(isEnable)
    }
}