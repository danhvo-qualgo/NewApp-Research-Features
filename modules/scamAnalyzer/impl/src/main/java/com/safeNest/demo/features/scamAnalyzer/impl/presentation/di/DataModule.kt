package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.impl.data.extractor.RegexEntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.OnDeviceAnalyzerRepository
import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.ScamAnalyzerRepositoryImpl
import com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor.EntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.ScamAnalyzerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class DataModule {

    @Provides
    @ActivityRetainedScoped
    fun scamAnalyzerRepository(impl: ScamAnalyzerRepositoryImpl): ScamAnalyzerRepository = impl
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds
    @Singleton
    abstract fun bindAnalyzeRepository(
        impl: OnDeviceAnalyzerRepository
    ): AnalyzerRepository

    @Binds
    @Singleton
    abstract fun bindEntityExtractor(
        impl: RegexEntityExtractor
    ): EntityExtractor
}