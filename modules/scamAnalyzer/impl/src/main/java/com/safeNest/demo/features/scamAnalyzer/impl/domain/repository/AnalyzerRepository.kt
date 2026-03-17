package com.safeNest.demo.features.scamAnalyzer.impl.domain.repository

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult

interface AnalyzeRepository {
    suspend fun analyze(input: AnalysisInput): AnalysisResult
}