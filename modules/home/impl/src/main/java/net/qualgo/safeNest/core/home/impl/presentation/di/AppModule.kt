package net.qualgo.safeNest.core.home.impl.presentation.di

import com.uney.core.router.Router
import net.qualgo.safeNest.core.home.api.HomeProvider
import net.qualgo.safeNest.core.home.impl.presentation.HomeProviderImpl
import net.qualgo.safeNest.core.home.impl.presentation.router.HomeRouter
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