package com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeAndGetResultUseCase
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerResultRepository
import javax.inject.Inject

class AnalyzeAndGetResultUseCaseImpl @Inject constructor(
    private val analyzeUseCase: AnalyzeUseCase,
    private val analyzerResultRepository: AnalyzerResultRepository
) : AnalyzeAndGetResultUseCase {
    override suspend fun invoke(input: AnalysisInput): Result<AnalysisResult> {
        return analyzeUseCase(input).mapCatching { resultKey ->
            analyzerResultRepository.getCachedResult(resultKey)
                ?: throw IllegalStateException("Analysis result not found for key: $resultKey")
        }
    }
}
