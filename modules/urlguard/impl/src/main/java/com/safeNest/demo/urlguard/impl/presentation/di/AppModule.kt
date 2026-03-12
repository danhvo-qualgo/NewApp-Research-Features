package com.safeNest.demo.urlguard.impl.presentation.di

import com.safeNest.demo.urlguard.api.UrlGuardProvider
import com.safeNest.demo.urlguard.impl.presentation.UrlGuardProviderImpl
import com.safeNest.demo.urlguard.impl.presentation.router.UrlGuardRouter
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
    fun provideFeatureProvider(impl: UrlGuardProviderImpl): UrlGuardProvider = impl

    @IntoSet
    @Provides
    fun providerFeatureRouter(impl: UrlGuardRouter): Router = impl
}