package com.safeNest.demo.features.scamAnalyzer.api.useCase

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult

interface GetAnalysisResultUseCase {
    suspend operator fun invoke(key: String): AnalysisResult?
}
