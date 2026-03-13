package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.impl.data.repository.ScamAnalyzerRepositoryImpl
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.ScamAnalyzerRepository
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