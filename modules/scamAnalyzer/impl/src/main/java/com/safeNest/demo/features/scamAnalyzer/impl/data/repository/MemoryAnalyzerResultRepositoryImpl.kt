package com.safeNest.demo.features.scamAnalyzer.impl.data.repository

import androidx.collection.LruCache
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerResultRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryAnalyzerResultRepositoryImpl @Inject constructor() : AnalyzerResultRepository {

    private val lruCache: LruCache<String, AnalysisResult> = LruCache(100)

    override suspend fun cacheResult(result: AnalysisResult): String {
        val key = UUID.randomUUID().toString()
        lruCache.put(key, result)
        return key
    }

    override suspend fun getCachedResult(key: String): AnalysisResult? {
        return lruCache[key]
    }
}