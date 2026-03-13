package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.uney.core.router.Router
import com.safeNest.demo.features.scamAnalyzer.api.ScamAnalyzerProvider
import com.safeNest.demo.features.scamAnalyzer.impl.presentation.ScamAnalyzerProviderImpl
import com.safeNest.demo.features.scamAnalyzer.impl.presentation.router.ScamAnalyzerRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {
    @Provides
    fun provideScamAnalyzerProvider(impl: ScamAnalyzerProviderImpl): ScamAnalyzerProvider = impl

    @IntoSet
    @Provides
    fun providerScamAnalyzerRouter(impl: ScamAnalyzerRouter): Router = impl
}