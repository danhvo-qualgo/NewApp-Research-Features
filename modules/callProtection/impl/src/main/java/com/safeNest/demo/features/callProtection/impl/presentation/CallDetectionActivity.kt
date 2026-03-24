package com.safeNest.demo.features.callProtection.impl.presentation

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.impl.presentation.navigator.Screen
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink.DATA_PAYLOAD
import com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist.add.AddBlockPatternScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.CallProtectionScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.MakeCallConfirmScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.MissingCallScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.ReviewCallScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.add.AddWhitelistScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class CallDetectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndClearNotification(intent = intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            AppNav3Host()
        }
    }
    override fun onResume() {
        super.onResume()

    }

    private fun checkAndClearNotification(intent: Intent) {
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", -1)
        if (notificationId != -1) {
            NotificationManagerCompat.from(this).cancel(notificationId)
            intent.removeExtra("EXTRA_NOTIFICATION_ID")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkAndClearNotification(intent)
    }

    @Composable
    private fun AppNav3Host() {
        val page = intent?.data?.getQueryParameter(CallDetectionDeeplink.PAGE) ?: CallDetectionDeeplink.BLOCKLIST
        val rootScreen = createRootScreen(page)
        val backStack = remember { mutableStateListOf<Any>(rootScreen) }
        val onBack: () -> Unit = { backStack.removeLastOrNull() }
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() }
        ) { key ->
            when (key) {
                is Screen.Home -> NavEntry(key) {
                    CallProtectionScreen(
                        tabName = page,
                        onBack = {
                            finish()
                        },
                        onAddToWhitelist = {
                            backStack.add(Screen.AddWhitelist)
                        },
                        onAddToBlacklist = {
                            backStack.add(Screen.AddBlocklist)
                        }
                    )
                }
                is Screen.AddBlocklist -> NavEntry(key) {
                    AddBlockPatternScreen {
                        onBack()
                    }
                }
                is Screen.AddWhitelist -> NavEntry(key) {
                    AddWhitelistScreen {
                        onBack()
                    }
                }

                is Screen.MissingCall -> NavEntry(key){
                    MissingCallScreen(
                        callerIdInfo = key.callerIdInfo,
                        onBack = {
                            finish()
                        },
                        onCallback = {
                            backStack.add(Screen.MakeCallConfirm(key.callerIdInfo))
                        }
                    )
                }

                is Screen.MakeCallConfirm -> NavEntry(key){
                    MakeCallConfirmScreen(
                        key.callerIdInfo,
                        onGoToReview = {
                            backStack.add(Screen.ReviewCall(key.callerIdInfo))
                        },
                        onBack = onBack
                    )
                }

                is Screen.ReviewCall -> NavEntry(key){
                    ReviewCallScreen(
                        callerIdInfo = key.callerIdInfo,
                        onBack = {
                            finish()
                        }
                    )
                }

                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        }
    }

    private fun createRootScreen(page: String): Screen {
        val payload = getPayload(intent.data!!)
        return when (page) {
            CallDetectionDeeplink.MISSING_CALL ->
                payload?.let {
                    Screen.MissingCall(Json.decodeFromString(it))
                } ?: Screen.Home(CallDetectionDeeplink.BLOCKLIST)
            CallDetectionDeeplink.REVIEW_CALL ->
                payload?.let {
                    Screen.ReviewCall(Json.decodeFromString(it))
                } ?: Screen.Home(CallDetectionDeeplink.BLOCKLIST)

            else -> {
                Screen.Home(page)
            }
        }
    }

    fun getPayload(uri: Uri): String? = Uri.decode(uri.getQueryParameter(DATA_PAYLOAD))

}