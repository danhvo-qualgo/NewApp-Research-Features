package com.safeNest.features.core.home.impl.presentation.di

import com.safeNest.features.core.home.impl.data.repository.HomeRepositoryImpl
import com.safeNest.features.core.home.impl.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class DataModule {
    @Binds
    @ViewModelScoped
    abstract fun provideHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}