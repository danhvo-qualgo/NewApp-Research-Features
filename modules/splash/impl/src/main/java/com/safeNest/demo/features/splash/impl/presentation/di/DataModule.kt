package com.safeNest.demo.features.splash.impl.presentation.di

import com.safeNest.demo.features.splash.impl.data.handler.AccessibilityServicePermissionHandler
import com.safeNest.demo.features.splash.impl.data.handler.DisplayOverAppsPermissionHandler
import com.safeNest.demo.features.splash.impl.data.handler.MicrophonePermissionHandler
import com.safeNest.demo.features.splash.impl.data.handler.NotificationListenerPermissionHandler
import com.safeNest.demo.features.splash.impl.data.handler.PhoneAndContactsPermissionHandler
import com.safeNest.demo.features.splash.impl.data.repository.SplashRepositoryImpl
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.repository.SplashRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class DataModule {

    @Provides
    @ActivityRetainedScoped
    fun splashRepository(impl: SplashRepositoryImpl): SplashRepository = impl

    @Provides
    @IntoSet
    fun provideDisplayOverAppsHandler(
        impl: DisplayOverAppsPermissionHandler,
    ): PermissionHandler = impl

    @Provides
    @IntoSet
    fun provideAccessibilityServiceHandler(
        impl: AccessibilityServicePermissionHandler,
    ): PermissionHandler = impl

    @Provides
    @IntoSet
    fun provideNotificationListenerHandler(
        impl: NotificationListenerPermissionHandler,
    ): PermissionHandler = impl

    @Provides
    @IntoSet
    fun providePhoneAndContactsHandler(
        impl: PhoneAndContactsPermissionHandler,
    ): PermissionHandler = impl

    @Provides
    @IntoSet
    fun provideMicrophoneHandler(
        impl: MicrophonePermissionHandler,
    ): PermissionHandler = impl
}