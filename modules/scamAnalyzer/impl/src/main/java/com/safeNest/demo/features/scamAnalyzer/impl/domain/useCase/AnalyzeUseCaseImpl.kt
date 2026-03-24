package com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase

import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.data.store.AnalyzeStore
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerResultRepository
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.OnDeviceSource
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.RemoteSource
import com.uney.core.network.api.models.ApiResult
import javax.inject.Inject

class AnalyzeUseCaseImpl @Inject constructor(
    private val analyzeStore: AnalyzeStore,
    @OnDeviceSource
    private val onDeviceAnalyzerRepository: AnalyzerRepository,
    @RemoteSource
    private val remoteAnalyzerRepository: AnalyzerRepository,
    private val analyzerResultRepository: AnalyzerResultRepository,
) : AnalyzeUseCase {
    override suspend fun invoke(input: AnalysisInput): Result<String> {
        return runCatching {
            val mode = analyzeStore.getMode()

            Log.d("AnalyzeUseCase", "Mode: $mode")

            val repository = when (mode) {
                AnalyzeMode.Local -> onDeviceAnalyzerRepository
                AnalyzeMode.Remote -> remoteAnalyzerRepository
            }

            val result = when (input) {
                is AnalysisInput.Audio -> repository.analyzeAudio(input.uri)
                is AnalysisInput.Image -> repository.analyzeImage(input.uri)
                is AnalysisInput.Text -> repository.analyzeText(
                    input.bundle,
                    input.senderId,
                    input.text
                )

                is AnalysisInput.Url -> repository.analyzeUrl(input.url)
            }

            val castResult = result as? ApiResult.Success<AnalysisResult> ?: return Result.failure(
                Exception("Failed to analyze response")
            )

            val cacheKey = analyzerResultRepository.cacheResult(castResult.data)
            cacheKey
        }
    }
}