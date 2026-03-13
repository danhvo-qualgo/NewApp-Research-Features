package com.safeNest.demo.features.splash.impl.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safeNest.demo.features.baseApp.base.activity.BaseActivity
import com.safeNest.demo.features.splash.impl.presentation.screen.permissions.PermissionsScreen
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
            val navController = rememberNavController()
            val activity = LocalActivity.current ?: return@CompositionLocalProvider
            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(
                        onNavigateToRequestPermission = {
                            navController.navigate("permission") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("permission") {
                    PermissionsScreen(
                        onStartClick = {
                            routerManager.navigate(activity, "internal://featureHome".toUri())
                        }
                    )
                }
            }

        }
    }
}
