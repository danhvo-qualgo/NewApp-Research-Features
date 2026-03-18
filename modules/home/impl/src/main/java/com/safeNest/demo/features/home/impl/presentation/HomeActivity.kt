package com.safeNest.demo.features.home.impl.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.designSystem.theme.DSTheme
import com.safeNest.demo.features.home.impl.presentation.ui.home.HomeScreen
import com.safeNest.demo.features.scamAnalyzer.api.router.ScamAnalyzerDeepLink
import com.safeNest.demo.features.urlGuard.api.UrlGuardProvider
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @Inject
    lateinit var routerManager: RouterManager

    @Inject
    lateinit var urlGuardProvider: UrlGuardProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        urlGuardProvider.startService(this)
        setContent {
            CompositionLocalProvider(LocalRouterManager provides routerManager) {
                DSTheme {
                    HomeScreen(
                        onManageProtectionClick = {
                            routerManager.navigate(
                                this@HomeActivity,
                                CallDetectionDeeplink.entryPoint()
                            )
                        },
                        onScamAnalyzerClick = {
                            routerManager.navigate(
                                this@HomeActivity,
                                ScamAnalyzerDeepLink.entryPoint()
                            )
                        }
                    )
                }
            }
        }
    }
}