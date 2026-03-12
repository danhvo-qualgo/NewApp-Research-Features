package com.safeNest.demo.features.home.impl.domain.presentation.di

import com.safeNest.demo.features.home.api.HomeProvider
import com.safeNest.demo.features.home.impl.domain.presentation.HomeProviderImpl
import com.safeNest.demo.features.home.impl.domain.presentation.router.HomeRouter
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
    fun provideFeatureProvider(impl: HomeProviderImpl): HomeProvider = impl

    @IntoSet
    @Provides
    fun providerFeatureRouter(impl: HomeRouter): Router = impl
}