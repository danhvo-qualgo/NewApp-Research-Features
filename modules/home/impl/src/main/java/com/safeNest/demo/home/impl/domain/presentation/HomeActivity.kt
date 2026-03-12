package com.safeNest.demo.home.impl.domain.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safeNest.demo.call.main.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.notificationInterceptor.api.presentation.router.NotificationInterceptorDeeplink
import com.safeNest.demo.permissionmanager.api.presentation.router.PermissionManagerDeeplink
import com.safeNest.demo.phishingDetection.api.presentation.router.PhishingDetectionDeeplink
import com.safeNest.demo.urlguard.api.presentation.router.UrlGuardDeeplink
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
                HomeScreen()
            }
        }
    }

    @Composable
    private fun HomeScreen() {
        Scaffold(Modifier.fillMaxSize()) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Home screen",
                    style = MaterialTheme.typography.titleLarge
                )

                Button(
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            CallDetectionDeeplink.entryPoint()
                        )
                    }
                ) { Text("Call Detection") }


                Button(
                    onClick = {
                        routerManager.navigate(this@HomeActivity, UrlGuardDeeplink.entryPoint())
                    }
                ) { Text("Url Guard") }

                Button(
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            PermissionManagerDeeplink.entryPoint()
                        )
                    }
                ) { Text("PermissionManager") }

                Button(
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            PhishingDetectionDeeplink.entryPoint()
                        )
                    }
                ) { Text("Phishing Detection") }

                Button(
                    onClick = {
                        routerManager.navigate(
                            this@HomeActivity,
                            NotificationInterceptorDeeplink.entryPoint()
                        )
                    }
                ) { Text("Notification Interceptor") }
            }
        }
    }
}