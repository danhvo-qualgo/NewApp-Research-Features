package com.safeNest.features.core.app

import com.uney.core.remoteConfig.api.RemoteConfig
import com.uney.core.remoteConfig.api.RemoteConfigManager
import net.qualgo.safeNest.core.baseApp.base.app.BaseApplication
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : BaseApplication() {
    @Inject
    internal lateinit var remoteConfig: dagger.Lazy<RemoteConfig>

    override fun onCreate() {
        super.onCreate()

        RemoteConfigManager.update(remoteConfig.get())
    }
}