package com.safeNest.features.call.callDetection.impl.domain.repository

import com.safeNest.features.call.callDetection.impl.domain.model.BlacklistPattern
import kotlinx.coroutines.flow.Flow

interface BlacklistPatternRepository {
    fun getBlacklistPatterns(): Flow<List<BlacklistPattern>>
    suspend fun add(pattern: BlacklistPattern)
    suspend fun remove(pattern: String)

    fun isEnable(): Flow<Boolean>
    suspend fun setEnable(isEnable: Boolean)
}