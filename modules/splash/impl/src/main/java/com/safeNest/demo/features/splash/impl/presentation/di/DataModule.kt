package com.safeNest.demo.features.splash.impl.presentation.di

import com.safeNest.demo.features.splash.impl.data.repository.SplashRepositoryImpl
import com.safeNest.demo.features.splash.impl.domain.repository.SplashRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class DataModule {

    @Provides
    @ActivityRetainedScoped
    fun splashRepository(impl: SplashRepositoryImpl): SplashRepository = impl
}