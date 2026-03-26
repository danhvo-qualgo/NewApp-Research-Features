package com.safeNest.demo.features.splash.impl.domain.usecase

import com.safeNest.demo.features.splash.impl.data.datastore.SplashDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageDnsPermissionUseCase @Inject constructor(
    private val splashDataStore: SplashDataStore
) {
    fun getDnsPermissionStateFlow(): Flow<Boolean> = splashDataStore.getDsnPermissionFlow()
    suspend fun setDnsPermissionState(enable: Boolean) = splashDataStore.setDnsPermission(enable)
    suspend fun getDnsPermissionState(): Boolean = splashDataStore.isDnsPermissionGranted()

}