package net.qualgo.safeNest.features.phishingDetection.impl.presentation.di

import com.uney.core.router.Router
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import net.qualgo.safeNest.features.phishingDetection.api.PhishingDetectionProvider
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.AppModelStorage
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr.AppWhisperModelStorage
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.ModelStorage
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr.WhisperModelStorage
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.urlChecker.PhishingDetectionProviderImpl
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.router.PhishingDetectionRouter

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppModule {

    @Binds
    abstract fun bindModelStorage(impl: AppModelStorage): ModelStorage

    @Binds
    abstract fun bindWhisperModelStorage(impl: AppWhisperModelStorage): WhisperModelStorage

    companion object {
        @Provides
        fun provideFeatureProvider(impl: PhishingDetectionProviderImpl): PhishingDetectionProvider = impl

        @IntoSet
        @Provides
        fun providerFeatureRouter(impl: PhishingDetectionRouter): Router = impl
    }
}