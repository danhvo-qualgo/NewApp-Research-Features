package com.safeNest.demo.features.phishingDetection.impl.presentation.di

import com.safeNest.demo.features.phishingDetection.api.PhishingDetectionProvider
import com.safeNest.demo.features.phishingDetection.impl.presentation.AppModelStorage
import com.safeNest.demo.features.phishingDetection.impl.presentation.ModelStorage
import com.safeNest.demo.features.phishingDetection.impl.presentation.PhishingDetectionProviderImpl
import com.safeNest.demo.features.phishingDetection.impl.presentation.router.PhishingDetectionRouter
import com.uney.core.router.Router
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppModule {

    @Binds
    abstract fun bindModelStorage(impl: AppModelStorage): ModelStorage

    companion object {
        @Provides
        fun provideFeatureProvider(impl: PhishingDetectionProviderImpl): PhishingDetectionProvider =
            impl

        @IntoSet
        @Provides
        fun providerFeatureRouter(impl: PhishingDetectionRouter): Router = impl
    }
}