package com.safeNest.demo.call.main.impl.domain.repository

import com.safeNest.demo.call.main.impl.domain.model.BlacklistPattern
import kotlinx.coroutines.flow.Flow

interface BlacklistPatternRepository {
    fun getBlacklistPatterns(): Flow<List<BlacklistPattern>>
    suspend fun add(pattern: String)
    suspend fun remove(pattern: String)

    fun isEnable(): Flow<Boolean>
    suspend fun setEnable(isEnable: Boolean)
}