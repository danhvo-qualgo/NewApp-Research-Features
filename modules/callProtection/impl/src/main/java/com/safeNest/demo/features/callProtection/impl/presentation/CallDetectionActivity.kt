package com.safeNest.demo.features.callProtection.impl.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.safeNest.demo.features.callProtection.impl.presentation.navigator.Screen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist.add.AddBlockPatternScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.CallProtectionScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.add.AddWhitelistScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallDetectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            AppNav3Host()
        }
    }

    var isFirst = false
    override fun onResume() {
        super.onResume()

    }

    @Composable
    private fun AppNav3Host() {
        val backStack = remember { mutableStateListOf<Any>(Screen.Home) }
        val onBack: () -> Unit = { backStack.removeLastOrNull() }
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() }
        ) { key ->
            when (key) {
                is Screen.Home -> NavEntry(key) {
                    CallProtectionScreen(
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

                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        }
    }
}