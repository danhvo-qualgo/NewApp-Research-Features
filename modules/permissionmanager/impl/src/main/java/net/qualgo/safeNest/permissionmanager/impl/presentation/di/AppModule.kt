package net.qualgo.safeNest.permissionmanager.impl.presentation.di

import net.qualgo.safeNest.permissionmanager.api.PermissionManagerProvider
import net.qualgo.safeNest.permissionmanager.impl.presentation.PermissionManagerProviderImpl
import net.qualgo.safeNest.permissionmanager.impl.presentation.router.PermissionManagerRouter
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
    fun provideFeatureProvider(impl: PermissionManagerProviderImpl): PermissionManagerProvider = impl

    @IntoSet
    @Provides
    fun providerFeatureRouter(impl: PermissionManagerRouter): Router = impl
}
