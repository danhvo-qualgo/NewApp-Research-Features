package net.qualgo.safeNest.core.authChallenge.impl.presentation.di

import net.qualgo.safeNest.core.authChallenge.api.AuthChallengeProvider
import net.qualgo.safeNest.core.authChallenge.impl.presentation.AuthChallengeProviderImpl
import net.qualgo.safeNest.core.authChallenge.impl.presentation.router.AuthChallengeRouter
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
    fun featureProvider(impl: AuthChallengeProviderImpl): AuthChallengeProvider = impl

    @IntoSet
    @Provides
    fun featureRouter(impl: AuthChallengeRouter): Router = impl
}