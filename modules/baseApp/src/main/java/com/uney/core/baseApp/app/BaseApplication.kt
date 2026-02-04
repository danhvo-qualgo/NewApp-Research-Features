package com.uney.core.baseApp.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.safeNest.core.remoteConfig.api.RemoteConfig
import com.uney.core.coreutils.android.callback.AppCallbackManager
import com.uney.core.logger.LoggerProvider
import com.uney.core.logger.LvLogger
import javax.inject.Inject

open class BaseApplication : Application(), Configuration.Provider {
    @Inject
    internal lateinit var workerFactory: dagger.Lazy<HiltWorkerFactory>

    @Inject
    internal lateinit var remoteConfig: dagger.Lazy<RemoteConfig>

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
        LvLogger.install(object : LoggerProvider {
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

//        RemoteConfigManager.update(remoteConfig.get())
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