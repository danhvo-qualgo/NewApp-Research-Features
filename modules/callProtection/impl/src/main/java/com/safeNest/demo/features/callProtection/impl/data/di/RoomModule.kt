package com.safeNest.demo.features.callProtection.impl.data.di

import android.content.Context
import androidx.room.Room
import com.safeNest.demo.features.callProtection.impl.data.local.CallDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RoomModule {

    @Singleton
    @Provides
    fun callDetectionDatabase(
        @ApplicationContext applicationContext: Context
    ): CallDataBase {
        return Room.databaseBuilder(
            applicationContext, CallDataBase::class.java, "CallDataBase"
        ).build()
    }

    @Singleton
    @Provides
    fun whitelistDao(database: CallDataBase) = database.whitelistDao()


    @Singleton
    @Provides
    fun blacklistPatternDao(database: CallDataBase) = database.blacklistPatternDao()
}