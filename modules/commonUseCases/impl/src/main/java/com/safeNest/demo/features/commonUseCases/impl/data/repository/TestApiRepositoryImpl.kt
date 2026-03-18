package com.safeNest.demo.features.commonUseCases.impl.data.repository

import com.safeNest.demo.features.commonUseCases.impl.domain.repository.TestApiRepository
import com.uney.core.network.api.ApiClient
import com.uney.core.network.api.configs.NonAuthClient
import com.uney.core.network.api.ext.post
import com.uney.core.network.api.models.ApiResult
import com.uney.core.utils.kotlin.result.DomainResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

class TestApiRepositoryImpl @Inject constructor(
    @NonAuthClient private val apiClient: ApiClient,
) : TestApiRepository {

    override suspend fun callApi(): DomainResult<String, String> = withContext(Dispatchers.IO) {
        val result = apiClient.post<SmsClarifyRequest, SmsClarifyResponse>(
            path = "api/v1.0/sms/classify",
            body = SmsClarifyRequest(
                entityMapping = buildJsonObject { },
                redactedMessage = "Ma OTP cua ban la 123456. Khong chia se ma nay voi bat ky ai."
            ),
        )
        when (result) {
            is ApiResult.Error -> {
                DomainResult.Error(result.error.detail)
            }

            is ApiResult.Exception -> {
                DomainResult.Error(result.throwable.message ?: "Unknown exception")
            }

            is ApiResult.Success<SmsClarifyResponse> -> {
                DomainResult.Success(
                    result.data.keyFindings?.firstOrNull()?.category ?: "No result"
                )
            }
        }
    }
}