package com.safeNest.demo.features.safeBrowsing.impl.presentation.di

import com.uney.core.router.Router
import com.safeNest.demo.features.safeBrowsing.api.SafeBrowsingProvider
import com.safeNest.demo.features.safeBrowsing.impl.presentation.SafeBrowsingProviderImpl
import com.safeNest.demo.features.safeBrowsing.impl.presentation.router.SafeBrowsingRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {
    @Provides
    fun provideSafeBrowsingProvider(impl: SafeBrowsingProviderImpl): SafeBrowsingProvider = impl

    @IntoSet
    @Provides
    fun providerSafeBrowsingRouter(impl: SafeBrowsingRouter): Router = impl
}