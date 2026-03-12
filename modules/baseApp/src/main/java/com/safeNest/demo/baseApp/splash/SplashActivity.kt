package com.safeNest.demo.baseApp.splash

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.safeNest.demo.baseApp.base.activity.BaseActivity
import com.uney.core.router.compose.LocalRouterManager
import com.uney.core.utils.android.qualifier.AppEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class SplashActivity : BaseActivity() {

    @Inject
    @AppEntryPoint
    lateinit var entryPoint: Uri

    override fun onCreateContent(savedInstanceState: Bundle?) {
        super.onCreateContent(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { provider -> provider.remove() }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
    }

    @Composable
    override fun Content(savedInstanceState: Bundle?) {
        CompositionLocalProvider(LocalRouterManager provides routerManager) {
            LoadingScreen(entryPoint)
        }
    }
}