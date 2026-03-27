package com.safeNest.demo.features.home.impl.data.repository

import com.safeNest.demo.features.home.impl.data.datastore.HomeDataStore
import com.safeNest.demo.features.home.impl.domain.repository.HomeRepository
import com.uney.core.network.api.DownloadClient
import com.uney.core.network.api.models.ApiResult
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeDataStore: HomeDataStore,
    private val downloadClient: DownloadClient
) : HomeRepository {
    override fun getDnsPermissionStateFlow(): Flow<Boolean> = homeDataStore.getDnsPermissionFlow()
    override suspend fun setDnsPermissionState(enable: Boolean) = homeDataStore.setDnsPermission(enable)
    override suspend fun getDnsPermissionState(): Boolean = homeDataStore.isDnsPermissionGranted()
    override suspend fun downloadCaCert(): Result<InputStream> {
        val result = downloadClient.getStream("https://demo-safenest-usecase.qualgo.dev/api/v1.0/dns/ca.pem")
        return when (result) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception("Error: ${result.error.detail}"))
            is ApiResult.Exception -> Result.failure(result.throwable)
        }
    }
}