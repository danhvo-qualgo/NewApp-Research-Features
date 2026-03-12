package com.safeNest.demo.features.call.impl.presentation.di

import com.safeNest.demo.features.call.impl.data.repository.BlacklistPatternRepositoryImpl
import com.safeNest.demo.features.call.impl.data.repository.CallDetectionRepositoryImpl
import com.safeNest.demo.features.call.impl.data.repository.WhitelistRepositoryImpl
import com.safeNest.demo.features.call.impl.domain.repository.BlacklistPatternRepository
import com.safeNest.demo.features.call.impl.domain.repository.CallDetectionRepository
import com.safeNest.demo.features.call.impl.domain.repository.WhitelistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class DataModule {

    @Provides
    fun callDetectionRepository(
        impl: CallDetectionRepositoryImpl
    ): CallDetectionRepository = impl

    @Singleton
    @Provides
    fun whitelistRepository(
        impl: WhitelistRepositoryImpl
    ): WhitelistRepository = impl

    @Singleton
    @Provides
    fun blacklistPatternRepository(
        impl: BlacklistPatternRepositoryImpl
    ): BlacklistPatternRepository = impl
}