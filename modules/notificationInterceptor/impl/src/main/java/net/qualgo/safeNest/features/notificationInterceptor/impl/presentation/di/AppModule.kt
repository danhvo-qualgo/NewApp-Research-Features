package net.qualgo.safeNest.features.notificationInterceptor.impl.presentation.di

import com.uney.core.router.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import net.qualgo.safeNest.features.notificationInterceptor.api.NotificationInterceptorProvider
import net.qualgo.safeNest.features.notificationInterceptor.impl.presentation.NotificationInterceptorProviderImpl
import net.qualgo.safeNest.features.notificationInterceptor.impl.presentation.router.NotificationInterceptorRouter

@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {
    @Provides
    fun provideFeatureProvider(impl: NotificationInterceptorProviderImpl): NotificationInterceptorProvider = impl

    @IntoSet
    @Provides
    fun providerFeatureRouter(impl: NotificationInterceptorRouter): Router = impl
}