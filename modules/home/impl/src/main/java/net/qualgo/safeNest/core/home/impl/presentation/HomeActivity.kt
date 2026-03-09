package net.qualgo.safeNest.core.home.impl.presentation

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
import com.example.api.presentation.router.UrlGuardDeeplink
import net.qualgo.safeNest.core.signIn.api.presentation.router.SignInDeeplink
import com.uney.core.router.compose.LocalRouterManager
import com.uney.core.router.RouterManager
import dagger.hilt.android.AndroidEntryPoint
import net.qualgo.safeNest.features.notificationInterceptor.api.presentation.router.NotificationInterceptorDeeplink
import net.qualgo.safeNest.features.phishingDetection.api.presentation.router.PhishingDetectionDeeplink
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
                        routerManager.navigate(this@HomeActivity, SignInDeeplink.entryPoint())
                    }
                ) { Text("Sign In") }


                Button(
                    onClick = {
                        routerManager.navigate(this@HomeActivity, UrlGuardDeeplink.entryPoint())
                    }
                ) { Text("Url Guard") }

                Button(
                    onClick = {
                        routerManager.navigate(this@HomeActivity, PhishingDetectionDeeplink.entryPoint())
                    }
                ) { Text("Phishing Detection") }

                Button(
                    onClick = {
                        routerManager.navigate(this@HomeActivity, NotificationInterceptorDeeplink.entryPoint())
                    }
                ) { Text("Notification Interceptor") }
            }
        }
    }
}