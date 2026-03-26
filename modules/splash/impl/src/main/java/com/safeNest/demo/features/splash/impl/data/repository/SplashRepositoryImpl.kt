package com.safeNest.demo.features.splash.impl.data.repository

import com.safeNest.demo.features.splash.impl.domain.repository.SplashRepository
import com.uney.core.network.api.DownloadClient
import com.uney.core.network.api.models.ApiResult
import jakarta.inject.Inject
import java.io.InputStream

class SplashRepositoryImpl @Inject constructor(
    private val downloadClient: DownloadClient
) : SplashRepository {
    override suspend fun downloadCaCert(): Result<InputStream> {
        val result = downloadClient.getStream("https://demo-safenest-usecase.qualgo.dev/api/v1.0/dns/ca.pem")
        return when(result) {
            is ApiResult.Success -> Result.success(result.data)
            is ApiResult.Error -> Result.failure(Exception("Error: ${result.error.detail}"))
            is ApiResult.Exception -> Result.failure(result.throwable)
        }
    }
}