package com.safeNest.demo.features.scamAnalyzer.impl.data.repository

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerResultRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryAnalyzerResultRepositoryImpl @Inject constructor() : AnalyzerResultRepository {
    private var cachedResult: AnalysisResult? = null

    override suspend fun cacheResult(result: AnalysisResult) {
        cachedResult = result
    }

    override suspend fun getCachedResult(): AnalysisResult? {
        return cachedResult
    }
}