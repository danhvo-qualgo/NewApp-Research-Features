package com.safeNest.demo.features.permissionManager.impl.presentation.di

import com.safeNest.demo.features.permissionManager.api.domain.GetAppPermissionInfoUseCase
import com.safeNest.demo.features.permissionManager.impl.domain.usecase.GetAppPermissionInfoUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Binds
    abstract fun bindGetAppPermissionInfoUseCase(
        impl: GetAppPermissionInfoUseCaseImpl
    ): GetAppPermissionInfoUseCase
}