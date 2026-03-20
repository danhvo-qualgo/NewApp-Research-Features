package com.safeNest.demo.features.scamAnalyzer.impl.presentation.di

import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import com.safeNest.demo.features.scamAnalyzer.api.useCase.GetAnalysisResultUseCase
import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageAnalyzeModeUseCase
import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageCustomPromptUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase.AnalyzeUseCaseImpl
import com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase.GetCachedAnalyzeUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase.ManageAnalyzeModeUseCaseImpl
import com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase.ManageCustomPromptUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
internal class DomainModule {
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Binds
    abstract fun bindAnalyzeUrlUseCase(
        impl: AnalyzeUseCaseImpl
    ): AnalyzeUseCase

    @Binds
    abstract fun bindGetAnalysisResultUseCase(
        impl: GetCachedAnalyzeUseCase
    ): GetAnalysisResultUseCase

    @Binds
    abstract fun bindManageAnalyzeModeUseCase(
        impl: ManageAnalyzeModeUseCaseImpl
    ): ManageAnalyzeModeUseCase
    
    @Binds
    abstract fun bindManageCustomPromptUseCase(
        impl: ManageCustomPromptUseCaseImpl
    ): ManageCustomPromptUseCase
}