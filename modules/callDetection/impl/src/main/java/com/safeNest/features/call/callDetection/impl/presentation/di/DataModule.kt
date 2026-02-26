package com.safeNest.features.call.callDetection.impl.presentation.di

import com.safeNest.features.call.callDetection.impl.data.repository.CallDetectionRepositoryImpl
import com.safeNest.features.call.callDetection.impl.domain.repository.CallDetectionRepository
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
    fun callDetectionRepository(impl: CallDetectionRepositoryImpl): CallDetectionRepository = impl
}