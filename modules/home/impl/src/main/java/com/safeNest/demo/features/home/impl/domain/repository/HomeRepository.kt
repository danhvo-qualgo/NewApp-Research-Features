package com.safeNest.demo.features.home.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface HomeRepository {
    fun getDnsPermissionStateFlow(): Flow<Boolean>
    suspend fun setDnsPermissionState(enable: Boolean)
    suspend fun getDnsPermissionState(): Boolean
    suspend fun downloadCaCert(): Result<InputStream>
}