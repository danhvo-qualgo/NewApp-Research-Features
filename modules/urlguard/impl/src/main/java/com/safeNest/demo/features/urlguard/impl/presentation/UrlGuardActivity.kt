package com.safeNest.demo.features.urlguard.impl.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.safenest.urlguard.UrlGuardTheme
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UrlGuardActivity : ComponentActivity() {
    @Inject
    lateinit var routerManager: RouterManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            UrlGuardTheme {
                CompositionLocalProvider(LocalRouterManager provides routerManager) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    // ── "Display over other apps" permission ──────────────────────────────────
    var isOverlayGranted by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }
    // StartActivityForResult: re-check the flag when the user comes back from Settings
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { isOverlayGranted = Settings.canDrawOverlays(context) }

    // ── Notification permission (Android 13+) ─────────────────────────────────
    var notificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationPermissionGranted = granted }


    // ── Audio permission (for screen recording with microphone) ───────────────
    var isAudioGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> isAudioGranted = granted }

    // ── Storage permission (Android 9 and below only) ─────────────────────────
    var isStorageGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> isStorageGranted = granted }

    // ── Auto-request on first launch ──────────────────────────────────────────
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!isAudioGranted) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !isStorageGranted) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SafeNest UrlGuard",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enable the accessibility service to block malicious sites in Chrome and other browsers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // ── Accessibility service ──────────────────────────────────────────
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            ) { Text("Open Accessibility Settings") }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Find \"SafeNest UrlGuard\" and turn it on.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Display over other apps ────────────────────────────────────────
            if (!isOverlayGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        overlayPermissionLauncher.launch(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                ) { Text("Allow Display over Other Apps") }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Required to show the floating protection overlay on top of other apps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Notification permission ────────────────────────────────────────
            if (!notificationPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) { Text("Allow Notifications") }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notifications are used to alert you when a threat is blocked.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Audio permission (microphone for screen recording) ─────────────
            if (!isAudioGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) { Text("Allow Microphone") }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Required to record audio while capturing the screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Screen-capture permission ──────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))

            // ── Storage permission (API ≤ 28 only) ─────────────────────────────
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !isStorageGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                ) { Text("Allow Storage Access") }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Required to save screenshots to your gallery on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
