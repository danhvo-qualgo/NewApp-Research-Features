package com.safeNest.demo.features.scamAnalyzer.api.useCase

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput

interface AnalyzeUseCase {
    suspend operator fun invoke(input: AnalysisInput): Result<String>
}