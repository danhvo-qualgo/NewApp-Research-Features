package com.safeNest.demo.baseApp.base.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.uney.core.logger.AppLogger
import com.uney.core.logger.AppLoggerProvider
import com.uney.core.remoteConfig.api.RemoteConfig
import com.uney.core.remoteConfig.api.RemoteConfigManager
import com.uney.core.utils.android.callback.AppCallbackManager
import dagger.Lazy
import javax.inject.Inject

open class BaseApplication : Application(), Configuration.Provider {

    @Inject
    internal lateinit var workerFactory: Lazy<HiltWorkerFactory>

    @Inject
    internal lateinit var remoteConfig: Lazy<RemoteConfig>

    @Inject
    internal lateinit var appCallbackManager: AppCallbackManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory.get()).build()

    override fun onCreate() {
        super.onCreate()

        initializeLibraries()

        registerProcessLifecycleOwner()

        appCallbackManager.onCreate()
    }

    private fun initializeLibraries() {
        AppLogger.install(object : AppLoggerProvider {
            override fun d(message: String) {
                Log.d("TAG", message)
            }

            override fun i(message: String) {
                Log.i("TAG", message)
            }

            override fun e(message: String, throwable: Throwable?) {
                Log.e("TAG", message, throwable)
            }
        })

        RemoteConfigManager.update(remoteConfig.get())
    }

    private fun registerProcessLifecycleOwner() {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                appCallbackManager.onAppForeground()
            }

            override fun onStop(owner: LifecycleOwner) {
                appCallbackManager.onAppBackground()
            }
        })
    }
}