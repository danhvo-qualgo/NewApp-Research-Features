package com.safeNest.demo.features.splash.impl.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.safeNest.demo.features.baseApp.base.activity.BaseActivity
import com.safeNest.demo.features.splash.impl.presentation.screen.splash.SplashScreen
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SplashActivity : BaseActivity() {

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
            SplashScreen()
        }
    }
}