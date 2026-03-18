package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase.AnalyzeUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal class DomainModule {
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {
    @Binds
    abstract fun bindAnalyzeUrlUseCase(
        impl: AnalyzeUseCaseImpl
    ): AnalyzeUseCase
}