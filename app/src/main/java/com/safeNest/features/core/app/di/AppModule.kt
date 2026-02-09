package com.safeNest.features.core.app.di

import android.net.Uri
import androidx.core.net.toUri
import com.uney.core.utils.android.qualifier.AppEntryPoint
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @AppEntryPoint
    fun provideEntryPoint(): Uri {
        return "internal://home".toUri()
    }
}