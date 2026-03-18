package com.safeNest.demo.features.callProtection.impl.presentation.di

import com.safeNest.demo.features.callProtection.api.domain.model.GetCallerIdInfoUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetCallerIdUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class DomainModule {

    @Singleton
    @Provides
    fun getCallerIdInfoUseCase(
        impl: GetCallerIdUseCaseImpl
    ): GetCallerIdInfoUseCase = impl
}