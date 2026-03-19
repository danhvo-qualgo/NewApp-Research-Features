package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.api.ScamAnalyzerProvider
import com.safeNest.demo.features.scamAnalyzer.impl.presentation.ScamAnalyzerProviderImpl
import com.safeNest.demo.features.scamAnalyzer.impl.presentation.router.ScamAnalyzerRouter
import com.safeNest.demo.features.scamAnalyzer.impl.utils.AppModelStorage
import com.safeNest.demo.features.scamAnalyzer.impl.utils.ModelStorage
import com.safeNest.demo.features.scamAnalyzer.impl.utils.asr.AppWhisperModelStorage
import com.safeNest.demo.features.scamAnalyzer.impl.utils.asr.WhisperModelStorage
import com.uney.core.router.Router
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

    @Provides
    fun provideModelStorage(impl: AppModelStorage): ModelStorage = impl

    @Provides
    fun provideWhisperModelStorage(impl: AppWhisperModelStorage): WhisperModelStorage = impl
}