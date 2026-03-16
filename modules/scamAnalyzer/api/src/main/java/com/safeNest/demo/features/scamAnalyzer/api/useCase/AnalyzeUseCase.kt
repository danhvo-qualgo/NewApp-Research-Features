package com.safeNest.demo.features.scamAnalyzer.api.useCase

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult

interface AnalyzeUseCase {
    suspend operator fun invoke(input: AnalysisInput): AnalysisResult
}