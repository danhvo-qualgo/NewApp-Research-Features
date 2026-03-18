package com.safeNest.demo.features.commonUseCases.impl.presentation.di

import com.safeNest.demo.features.commonUseCases.api.TestApiUseCase
import com.safeNest.demo.features.commonUseCases.impl.data.repository.TestApiRepositoryImpl
import com.safeNest.demo.features.commonUseCases.impl.domain.repository.TestApiRepository
import com.safeNest.demo.features.commonUseCases.impl.domain.usecase.TestApiUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonUseCasesModule {

    /**
     * Repository
     */

    @Binds
    abstract fun testApiRepository(impl: TestApiRepositoryImpl): TestApiRepository

    /**
     * Use Case
     */

    @Binds
    abstract fun testApiUseCase(impl: TestApiUseCaseImpl): TestApiUseCase
}