package com.safeNest.features.core.home.impl.presentation.di

import com.uney.core.router.Router
import com.safeNest.features.core.home.api.HomeProvider
import com.safeNest.features.core.home.impl.presentation.HomeProviderImpl
import com.safeNest.features.core.home.impl.presentation.router.HomeRouter
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