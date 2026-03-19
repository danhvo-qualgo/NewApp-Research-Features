package com.safeNest.demo.features.scamAnalyzer.impl.domain.repository

import android.net.Uri
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.uney.core.network.api.models.ApiResult
import javax.inject.Qualifier

interface AnalyzerRepository {
    suspend fun analyzeAudio(uri: Uri): ApiResult<AnalysisResult>

    suspend fun analyzeUrl(url: String): ApiResult<AnalysisResult>

    suspend fun analyzeText(
        bundleId: String,
        sender: String,
        text: String
    ): ApiResult<AnalysisResult>

    suspend fun analyzeImage(uri: Uri): ApiResult<AnalysisResult>
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OnDeviceSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteSource