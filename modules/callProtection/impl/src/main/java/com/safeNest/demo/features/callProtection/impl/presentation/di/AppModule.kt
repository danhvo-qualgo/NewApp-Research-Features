package com.safeNest.demo.features.callProtection.impl.presentation.di

import com.safeNest.demo.features.callProtection.api.CallDetectionProvider
import com.safeNest.demo.features.callProtection.impl.presentation.CallDetectionProviderImpl
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionRouter
import com.safeNest.demo.features.callProtection.impl.presentation.service.handler.CallDetectionHandler
import com.safeNest.demo.features.callProtection.impl.presentation.service.handler.CallDetectionHandlerImpl
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
    fun provideCallDetectionProvider(impl: CallDetectionProviderImpl): CallDetectionProvider = impl

    @IntoSet
    @Provides
    fun providerCallDetectionRouter(impl: CallDetectionRouter): Router = impl

    @Provides
    fun providerCallDetectionHandler(impl: CallDetectionHandlerImpl): CallDetectionHandler = impl
}