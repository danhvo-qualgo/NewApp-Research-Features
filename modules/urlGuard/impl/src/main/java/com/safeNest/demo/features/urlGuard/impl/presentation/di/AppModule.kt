package com.safeNest.demo.features.urlGuard.impl.presentation.di

import android.content.Context
import com.safeNest.demo.features.permissionManager.api.domain.GetAppPermissionInfoUseCase
import com.safeNest.demo.features.urlGuard.api.UrlGuardProvider
import com.safeNest.demo.features.urlGuard.impl.detection.UrlDetection
import com.safeNest.demo.features.urlGuard.impl.detection.UrlDetectionImpl
import com.safeNest.demo.features.urlGuard.impl.presentation.UrlGuardProviderImpl
import com.safeNest.demo.features.urlGuard.impl.presentation.router.UrlGuardRouter
import com.safeNest.demo.features.urlGuard.impl.urlGuard.AppTrustChecker
import com.safenest.gate1.Gate1Classifier
import com.uney.core.router.Router
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    fun provideGate1Classifier(
        @ApplicationContext context: Context
    ): Gate1Classifier = Gate1Classifier(context)

    @Provides
    fun provideAppTrustChecker(
        @ApplicationContext context: Context,
        getAppPermissionInfoUseCase: GetAppPermissionInfoUseCase
    ): AppTrustChecker = AppTrustChecker(context, getAppPermissionInfoUseCase)

}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppModuleBind {
    @Binds
    abstract fun bindUrlDetection(impl: UrlDetectionImpl): UrlDetection
}
