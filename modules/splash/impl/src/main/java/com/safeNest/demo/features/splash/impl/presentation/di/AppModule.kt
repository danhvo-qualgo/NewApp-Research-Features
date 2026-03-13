package com.safeNest.demo.features.splash.impl.presentation.di

import com.uney.core.router.Router
import com.safeNest.demo.features.splash.api.SplashProvider
import com.safeNest.demo.features.splash.impl.presentation.SplashProviderImpl
import com.safeNest.demo.features.splash.impl.presentation.router.SplashRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {
    @Provides
    fun provideSplashProvider(impl: SplashProviderImpl): SplashProvider = impl

    @IntoSet
    @Provides
    fun providerSplashRouter(impl: SplashRouter): Router = impl
}