package com.safeNest.demo.features.scamAnalyzer.impl.data.repository

import android.net.Uri
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResultType
import com.safeNest.demo.features.scamAnalyzer.impl.domain.models.AnalyzeTextResult
import com.safeNest.demo.features.scamAnalyzer.impl.domain.models.AnalyzeUrlResult
import com.safeNest.demo.features.scamAnalyzer.impl.domain.models.toAnalysisItem
import com.safeNest.demo.features.scamAnalyzer.impl.domain.models.toAnalysisStatus
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import com.uney.core.network.api.ApiClient
import com.uney.core.network.api.configs.NonAuthClient
import com.uney.core.network.api.ext.post
import com.uney.core.network.api.models.ApiResult
import javax.inject.Inject

class RemoteAnalyzerRepositoryImpl @Inject constructor(
    @NonAuthClient private val apiClient: ApiClient,
) : AnalyzerRepository {
    private fun <T : Any, R : Any> ApiResult<T>.map(mapper: (T) -> R): ApiResult<R> {
        return when (this) {
            is ApiResult.Error -> this
            is ApiResult.Exception -> this
            is ApiResult.Success -> ApiResult.Success(mapper(data), rawResponse)
        }
    }

    override suspend fun analyzeAudio(uri: Uri): ApiResult<AnalysisResult> {
        return ApiResult.Exception(Throwable())
    }

    override suspend fun analyzeUrl(url: String): ApiResult<AnalysisResult> {
        return apiClient.post<Map<String, String>, AnalyzeUrlResult>(
            path = "/api/v1.0/analyze/url",
            body = mapOf("url" to url)
        ).map {
            AnalysisResult(
                data = AnalysisResultType.Url(url),
                status = it.verdict.toAnalysisStatus(),
                keyFindings = it.keyFindings.map { finding -> finding.toAnalysisItem() }
            )
        }
    }

    override suspend fun analyzeText(
        bundleId: String,
        sender: String,
        text: String,
    ): ApiResult<AnalysisResult> {
        return apiClient.post<Map<String, String>, AnalyzeTextResult>(
            path = "/api/v1.0/analyze/notification",
            body = mapOf(
                "bundleId" to bundleId,
                "sender" to sender,
                "text" to text
            )
        ).map {
            AnalysisResult(
                data = AnalysisResultType.Text(text, it.redactedMessage),
                status = it.verdict.toAnalysisStatus(),
                keyFindings = it.keyFindings.map { finding -> finding.toAnalysisItem() }
            )
        }
    }

    override suspend fun analyzeImage(uri: Uri): ApiResult<AnalysisResult> {
        return ApiResult.Exception(Throwable())
    }
}