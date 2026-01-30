package com.safeNest.features.core.authChallenge.impl.presentation.di

import com.safeNest.features.core.authChallenge.api.domain.useCase.InitSignInEmailUseCase
import com.safeNest.features.core.authChallenge.api.domain.useCase.InitSignInSsoUseCase
import com.safeNest.features.core.authChallenge.impl.domain.useCase.InitSignInEmailUseCaseImpl
import com.safeNest.features.core.authChallenge.impl.domain.useCase.InitSignInSsoUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class DomainModule {

    @Binds
    @ViewModelScoped
    abstract fun initSignInEmailUseCase(impl: InitSignInEmailUseCaseImpl): InitSignInEmailUseCase

    @Binds
    @ViewModelScoped
    abstract fun initSignInSsoUseCase(impl: InitSignInSsoUseCaseImpl): InitSignInSsoUseCase
}