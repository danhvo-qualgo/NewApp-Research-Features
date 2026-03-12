package com.safeNest.demo.features.permissionmanager.impl.presentation.di

import com.safeNest.demo.features.permissionmanager.api.PermissionManagerProvider
import com.safeNest.demo.features.permissionmanager.impl.presentation.PermissionManagerProviderImpl
import com.safeNest.demo.features.permissionmanager.impl.presentation.router.PermissionManagerRouter
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
    fun provideFeatureProvider(
        impl: PermissionManagerProviderImpl
    ): PermissionManagerProvider = impl

    @IntoSet
    @Provides
    fun providerFeatureRouter(impl: PermissionManagerRouter): Router = impl
}
