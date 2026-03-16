package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.impl.data.extractor.RegexEntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.OnDeviceAnalyzer
import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.ScamAnalyzerRepositoryImpl
import com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor.EntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzeRepository
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.ScamAnalyzerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class DataModule {

    @Provides
    @ActivityRetainedScoped
    fun scamAnalyzerRepository(impl: ScamAnalyzerRepositoryImpl): ScamAnalyzerRepository = impl
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class BindModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindAnalyzeRepository(
        impl: OnDeviceAnalyzer
    ): AnalyzeRepository

    @Binds
    @ActivityRetainedScoped
    abstract fun bindEntityExtractor(
        impl: RegexEntityExtractor
    ): EntityExtractor
}