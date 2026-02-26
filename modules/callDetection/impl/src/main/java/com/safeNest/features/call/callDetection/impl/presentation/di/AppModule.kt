package com.safeNest.features.call.callDetection.impl.presentation.di

import com.uney.core.router.Router
import com.safeNest.features.call.callDetection.api.CallDetectionProvider
import com.safeNest.features.call.callDetection.impl.presentation.CallDetectionProviderImpl
import com.safeNest.features.call.callDetection.impl.presentation.router.CallDetectionRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {
    @Provides
    fun provideCallDetectionProvider(impl: CallDetectionProviderImpl): CallDetectionProvider = impl

    @IntoSet
    @Provides
    fun providerCallDetectionRouter(impl: CallDetectionRouter): Router = impl
}