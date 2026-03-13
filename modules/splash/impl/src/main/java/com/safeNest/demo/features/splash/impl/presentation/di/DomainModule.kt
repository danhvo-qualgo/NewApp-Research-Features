package com.safeNest.demo.features.splash.impl.presentation.di

import android.content.Context
import android.os.Handler
import com.safeNest.demo.features.splash.impl.domain.PermissionManager
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ViewModelComponent::class)
internal class DomainModule {

    @Provides
    @ActivityRetainedScoped
    fun providePermissionManager(
        @ApplicationContext context: Context,
        handler: Set<PermissionHandler>
    ): PermissionManager = PermissionManager(context, handler)

}