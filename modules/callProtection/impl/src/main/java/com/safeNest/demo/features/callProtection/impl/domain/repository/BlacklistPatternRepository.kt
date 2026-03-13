package com.safeNest.demo.features.callProtection.impl.domain.repository

import com.safeNest.demo.features.callProtection.impl.domain.model.BlacklistPattern
import kotlinx.coroutines.flow.Flow

interface BlacklistPatternRepository {
    fun getBlacklistPatterns(): Flow<List<BlacklistPattern>>
    suspend fun add(pattern: BlacklistPattern)
    suspend fun remove(pattern: String)

    fun isEnable(): Flow<Boolean>
    suspend fun setEnable(isEnable: Boolean)
}