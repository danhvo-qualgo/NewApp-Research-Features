package com.safeNest.demo.features.home.impl.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSTheme
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.notificationInterceptor.api.presentation.router.NotificationInterceptorDeeplink
import com.safeNest.demo.features.permissionmanager.api.presentation.router.PermissionManagerDeeplink
import com.safeNest.demo.features.phishingDetection.api.presentation.router.PhishingDetectionDeeplink
import com.safeNest.demo.features.urlguard.api.presentation.router.UrlGuardDeeplink
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @Inject
    lateinit var routerManager: RouterManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            CompositionLocalProvider(LocalRouterManager provides routerManager) {
                DSTheme {
                    HomeScreen()
                }
            }
        }
    }

    @Composable
    private fun HomeScreen() {
        Scaffold(Modifier.fillMaxSize()) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(gradientBackground)
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "SafeNest Security",
                    style = DSTypography.h2.bold,
                    color = DSColors.textActionActive
                )

                DSButton(
                    text = "Call Detection",
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            CallDetectionDeeplink.entryPoint()
                        )
                    }
                )

                DSButton(
                    text = "Url Guard",
                    onClick = {
                        routerManager.navigate(this@HomeActivity, UrlGuardDeeplink.entryPoint())
                    }
                )

                DSButton(
                    text = "PermissionManager",
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            PermissionManagerDeeplink.entryPoint()
                        )
                    }
                )

                DSButton(
                    text = "Phishing Detection",
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            PhishingDetectionDeeplink.entryPoint()
                        )
                    }
                )

                DSButton(
                    text = "Notification Interceptor",
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            NotificationInterceptorDeeplink.entryPoint()
                        )
                    }
                )
            }
        }
    }
}