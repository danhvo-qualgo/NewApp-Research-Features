package net.qualgo.safeNest.permissionmanager.impl.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import net.qualgo.safeNest.permissionmanager.PermissionManagerTheme
import net.qualgo.safeNest.permissionmanager.impl.presentation.screen.PermissionManagerScreen
import javax.inject.Inject

@AndroidEntryPoint
class PermissionManagerActivity : ComponentActivity() {
    @Inject
    lateinit var routerManager: RouterManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            PermissionManagerTheme {
                CompositionLocalProvider(LocalRouterManager provides routerManager) {
                    PermissionManagerScreen()
                }
            }
        }
    }
}
