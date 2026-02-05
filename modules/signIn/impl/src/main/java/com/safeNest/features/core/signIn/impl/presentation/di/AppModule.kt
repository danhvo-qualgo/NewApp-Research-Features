package com.safeNest.features.core.signIn.impl.presentation.di

import com.safeNest.features.core.signIn.api.SignInProvider
import com.safeNest.features.core.signIn.impl.presentation.SignInProviderImpl
import com.safeNest.features.core.signIn.impl.presentation.router.SignInRouter
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
    fun featureProvider(impl: SignInProviderImpl): SignInProvider = impl

    @IntoSet
    @Provides
    fun featureRouter(impl: SignInRouter): Router = impl
}