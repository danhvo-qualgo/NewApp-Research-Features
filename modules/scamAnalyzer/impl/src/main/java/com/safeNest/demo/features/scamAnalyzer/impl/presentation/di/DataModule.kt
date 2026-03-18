package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.impl.data.extractor.RegexEntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.MemoryAnalyzerResultRepositoryImpl
import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.OnDeviceAnalyzerRepositoryImpl
import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.RemoteAnalyzerRepositoryImpl
import com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor.EntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerResultRepository
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.OnDeviceSource
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.RemoteSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class DataModule {
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds
    @OnDeviceSource
    abstract fun bindAnalyzeRepository(
        impl: OnDeviceAnalyzerRepositoryImpl
    ): AnalyzerRepository

    @Binds
    abstract fun bindResultRepository(
        impl: MemoryAnalyzerResultRepositoryImpl
    ): AnalyzerResultRepository

    @Binds
    @RemoteSource
    abstract fun bindRemoteAnalyzeRepository(
        impl: RemoteAnalyzerRepositoryImpl
    ): AnalyzerRepository

    @Binds
    abstract fun bindEntityExtractor(
        impl: RegexEntityExtractor
    ): EntityExtractor
}