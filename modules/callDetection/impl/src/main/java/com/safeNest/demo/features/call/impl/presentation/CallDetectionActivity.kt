package com.safeNest.demo.features.call.impl.presentation

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
import com.safeNest.demo.features.call.impl.presentation.navigator.Screen
import com.safeNest.demo.features.call.impl.presentation.ui.blacklist.BlacklistScreen
import com.safeNest.demo.features.call.impl.presentation.ui.home.HomeScreen
import com.safeNest.demo.features.call.impl.presentation.ui.whitelist.WhitelistScreen
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
        val backStack =
            remember { mutableStateListOf<Any>(Screen.Home) }
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() }
        ) { key ->
            when (key) {
                is Screen.Home -> NavEntry(key) {
                    HomeScreen(
                        onGoToWhitelist = {
                            backStack.add(Screen.Whitelist)
                        },
                        onGoToBlacklist = {
                            backStack.add(Screen.Blacklist)
                        }
                    )
                }

                is Screen.Whitelist -> NavEntry(key) {
                    WhitelistScreen(onBack = {
                        backStack.removeLastOrNull()
                    })
                }

                is Screen.Blacklist -> NavEntry(key) {
                    BlacklistScreen(onBack = {
                        backStack.removeLastOrNull()
                    })
                }

                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        }
    }
}