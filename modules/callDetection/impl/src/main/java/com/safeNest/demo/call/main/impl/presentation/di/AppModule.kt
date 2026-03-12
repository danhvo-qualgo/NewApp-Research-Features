package com.safeNest.demo.call.main.impl.presentation.di

import com.safeNest.demo.call.main.api.CallDetectionProvider
import com.safeNest.demo.call.main.impl.presentation.CallDetectionProviderImpl
import com.safeNest.demo.call.main.impl.presentation.router.CallDetectionRouter
import com.safeNest.demo.call.main.impl.presentation.service.handler.CallDetectionHandler
import com.safeNest.demo.call.main.impl.presentation.service.handler.CallDetectionHandlerImpl
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