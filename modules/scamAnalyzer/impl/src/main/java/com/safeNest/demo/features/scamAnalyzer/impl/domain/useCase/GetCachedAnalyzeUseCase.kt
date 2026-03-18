package com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.useCase.GetAnalysisResultUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerResultRepository
import javax.inject.Inject

class GetCachedAnalyzeUseCase @Inject constructor(
    private val repository: AnalyzerResultRepository
) : GetAnalysisResultUseCase {
    override suspend operator fun invoke(): AnalysisResult? {
        return repository.getCachedResult()
    }
}