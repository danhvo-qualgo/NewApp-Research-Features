package com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase

import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import javax.inject.Inject

class AnalyzeUseCaseImpl @Inject constructor(
    private val repository: AnalyzerRepository
) : AnalyzeUseCase {
    override suspend fun invoke(input: AnalysisInput): AnalysisResult {
        Log.d("AnalyzeUseCase", "Analyzing text: $input")
        return repository.analyze(input)
    }
}