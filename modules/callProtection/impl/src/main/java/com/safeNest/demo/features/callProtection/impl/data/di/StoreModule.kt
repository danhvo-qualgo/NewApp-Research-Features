package com.safeNest.demo.features.callProtection.impl.data.di

import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStore
import com.safeNest.demo.features.callProtection.impl.data.local.CallDeviceStoreImpl
import com.uney.core.storage.api.DeviceStorage
import com.uney.core.storage.api.StorageManager
import com.uney.core.storage.api.UserStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object StoreModule {

    @Singleton
    @Provides
    fun callDeviceStore(
        storageManager: StorageManager
    ): DeviceStorage {
        return storageManager.createDeviceStore("app_config")
    }

    @Singleton
    @Provides
    fun callUserStore(
        storageManager: StorageManager
    ): UserStorage {
        return storageManager.createUserStore("app_config")
    }


    @Singleton
    @Provides
    fun createCallDeviceStore(
        callDeviceStoreImpl: CallDeviceStoreImpl
    ): CallDeviceStore {
        return callDeviceStoreImpl
    }
}