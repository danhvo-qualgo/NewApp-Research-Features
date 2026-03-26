package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.impl.presentation.router.ScamAnalyzerRouter
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
    @IntoSet
    @Provides
    fun providerScamAnalyzerRouter(impl: ScamAnalyzerRouter): Router = impl

    @Provides
    fun WhisperModelStorage(impl: AppWhisperModelStorage): WhisperModelStorage = impl
}