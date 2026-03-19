package com.safeNest.demo.features.scamAnalyzer.impl.domain.repository

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult

interface AnalyzerResultRepository {
    suspend fun cacheResult(result: AnalysisResult)

    suspend fun getCachedResult(): AnalysisResult?
}