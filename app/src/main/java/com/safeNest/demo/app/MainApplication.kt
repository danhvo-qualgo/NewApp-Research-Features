package com.safeNest.demo.app

import com.uney.core.remoteConfig.api.RemoteConfig
import com.uney.core.remoteConfig.api.RemoteConfigManager
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : com.safeNest.demo.baseApp.base.app.BaseApplication() {
    @Inject
    internal lateinit var remoteConfig: Lazy<RemoteConfig>

    override fun onCreate() {
        super.onCreate()

        RemoteConfigManager.update(remoteConfig.get())
    }
}