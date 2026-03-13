package com.safeNest.demo.features.splash.impl.presentation.di

import android.content.Context
import com.safeNest.demo.features.splash.impl.domain.PermissionManager
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
internal class DomainModule {

    @Provides
    fun providePermissionManager(
        @ApplicationContext context: Context,
        handler: Set<@JvmSuppressWildcards PermissionHandler>
    ): PermissionManager = PermissionManager(context, handler)

}