package com.safeNest.demo.app

import com.safeNest.demo.features.baseApp.base.app.BaseApplication
import com.uney.core.remoteConfig.api.RemoteConfig
import com.uney.core.remoteConfig.api.RemoteConfigManager
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : BaseApplication() {
    @Inject
    internal lateinit var remoteConfig: Lazy<RemoteConfig>

    override fun onCreate() {
        super.onCreate()

        RemoteConfigManager.update(remoteConfig.get())
    }
}