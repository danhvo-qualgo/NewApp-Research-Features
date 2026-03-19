package com.safeNest.demo.features.callProtection.impl.presentation.di

import com.safeNest.demo.features.callProtection.impl.data.repository.BlacklistPatternRepositoryImpl
import com.safeNest.demo.features.callProtection.impl.data.repository.CallDetectionRepositoryImpl
import com.safeNest.demo.features.callProtection.impl.data.repository.MasterBlocklistRepositoryImpl
import com.safeNest.demo.features.callProtection.impl.data.repository.MasterWhitelistRepositoryImpl
import com.safeNest.demo.features.callProtection.impl.data.repository.WhitelistRepositoryImpl
import com.safeNest.demo.features.callProtection.impl.domain.repository.BlacklistPatternRepository
import com.safeNest.demo.features.callProtection.impl.domain.repository.CallDetectionRepository
import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterBlocklistRepository
import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterWhitelistRepository
import com.safeNest.demo.features.callProtection.impl.domain.repository.WhitelistRepository
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

    @Singleton
    @Provides
    fun masterBlocklistRepository(
        impl: MasterBlocklistRepositoryImpl
    ): MasterBlocklistRepository = impl

    @Singleton
    @Provides
    fun masterWhitelistRepository(
        impl: MasterWhitelistRepositoryImpl
    ): MasterWhitelistRepository = impl
}