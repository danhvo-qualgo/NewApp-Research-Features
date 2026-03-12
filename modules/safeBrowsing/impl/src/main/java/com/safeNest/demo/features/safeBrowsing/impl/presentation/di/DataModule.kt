package com.safeNest.demo.features.safeBrowsing.impl.presentation.di

import com.safeNest.demo.features.safeBrowsing.impl.data.repository.SafeBrowsingRepositoryImpl
import com.safeNest.demo.features.safeBrowsing.impl.domain.repository.SafeBrowsingRepository
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
    fun safeBrowsingRepository(impl: SafeBrowsingRepositoryImpl): SafeBrowsingRepository = impl
}