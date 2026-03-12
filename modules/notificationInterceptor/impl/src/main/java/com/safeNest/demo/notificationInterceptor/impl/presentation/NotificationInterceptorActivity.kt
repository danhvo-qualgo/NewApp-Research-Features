package com.safeNest.demo.notificationInterceptor.impl.presentation

import android.Manifest
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import com.safeNest.demo.notificationInterceptor.impl.data.NotificationRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class NotificationInterceptorActivity : ComponentActivity() {
    @Inject
    lateinit var routerManager: RouterManager

    private val viewModel: NotificationInterceptorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            CompositionLocalProvider(LocalRouterManager provides routerManager) {
                NotificationInterceptorScreen(viewModel)
            }
        }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (flat.isNullOrEmpty()) return false
    val componentName = ComponentName(context, NotificationInterceptorService::class.java)
    return flat.split(":").any { component ->
        try {
            ComponentName.unflattenFromString(component) == componentName
        } catch (e: Exception) {
            false
        }
    }
}

@Composable
fun NotificationInterceptorScreen(viewModel: NotificationInterceptorViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var listenerEnabled by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var postNotifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Re-check every time the screen resumes (e.g. after returning from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerEnabled = isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Notification Interceptor",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (!listenerEnabled) {
                PermissionCard(
                    message = "Notification listener access is required to intercept notifications.",
                    buttonText = "Grant Notification Access"
                ) {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    listenerEnabled = isNotificationListenerEnabled(context)
                }
                return@Column
            }

            HistoryScreen(uiState)
        }
    }
}

@Composable
private fun PermissionCard(message: String, buttonText: String, onAction: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ComposeColor(0xFFF1F3F4),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onAction) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun HistoryScreen(uiState: NotificationUiState) {
    if (uiState.chatNotifications.isEmpty() && uiState.callNotifications.isEmpty() && uiState.otherNotifications.isEmpty() && uiState.smsNotifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No notifications intercepted yet.",
                color = ComposeColor.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (uiState.chatNotifications.isNotEmpty()) {
            item { SectionHeader("Chat") }
            items(uiState.chatNotifications) { record ->
                NotificationRow(record)
                Divider(color = ComposeColor(0xFFEEEEEE))
            }
        }
        if (uiState.smsNotifications.isNotEmpty()) {
            item { SectionHeader("SMS") }
            items(uiState.smsNotifications) { record ->
                NotificationRow(record)
                Divider(color = ComposeColor(0xFFEEEEEE))
            }
        }
        if (uiState.callNotifications.isNotEmpty()) {
            item { SectionHeader("Calls") }
            items(uiState.callNotifications) { record ->
                NotificationRow(record)
                Divider(color = ComposeColor(0xFFEEEEEE))
            }
        }
        if (uiState.otherNotifications.isNotEmpty()) {
            item { SectionHeader("Other") }
            items(uiState.otherNotifications) { record ->
                NotificationRow(record)
                Divider(color = ComposeColor(0xFFEEEEEE))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = ComposeColor(0xFF6B7280),
        modifier = Modifier
            .fillMaxWidth()
            .background(ComposeColor(0xFFF9FAFB))
            .padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotificationRow(record: NotificationRecord) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        // Header row: app · time · status chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.appName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (!record.title.isNullOrBlank()) {
                    Text(text = record.title, fontSize = 13.sp, color = ComposeColor(0xFF111827))
                }
            }
            Text(
                text = formatTime(record.timeMs),
                fontSize = 11.sp,
                color = ComposeColor(0xFF9CA3AF),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Preview line
        val preview = record.messages.lastOrNull()
            ?: record.bigText
            ?: record.text
            ?: record.subText
        if (!preview.isNullOrBlank()) {
            Text(
                text = preview,
                fontSize = 13.sp,
                color = ComposeColor(0xFF374151),
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Status chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 6.dp)
        ) {
            if (record.wasClicked) StatusChip("Clicked", ComposeColor(0xFFDCFCE7), ComposeColor(0xFF166534))
            if (record.isOngoing) StatusChip("Ongoing", ComposeColor(0xFFE0E7FF), ComposeColor(0xFF3730A3))
            if (record.isForegroundService) StatusChip("FG Service", ComposeColor(0xFFFEF3C7), ComposeColor(0xFF92400E))
        }

        // Expanded details
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailBlock(record)
        }
    }
}

@Composable
private fun StatusChip(label: String, bg: ComposeColor, fg: ComposeColor) {
    Surface(shape = RoundedCornerShape(4.dp), color = bg) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun DetailBlock(record: NotificationRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ComposeColor(0xFFF9FAFB), RoundedCornerShape(8.dp))
            .border(1.dp, ComposeColor(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DetailRow("Package", record.packageName)
        DetailRow("Notif ID", "${record.notificationId}${if (record.notificationTag != null) " / tag: ${record.notificationTag}" else ""}")
        record.notificationChannelId?.let { DetailRow("Channel", it) }

        if (!record.conversationTitle.isNullOrBlank()) DetailRow("Conversation", record.conversationTitle)
        if (!record.senderName.isNullOrBlank()) DetailRow("Sender", record.senderName)
        if (!record.subText.isNullOrBlank()) DetailRow("Sub-text", record.subText)
        if (!record.infoText.isNullOrBlank()) DetailRow("Info", record.infoText)
        if (!record.summaryText.isNullOrBlank()) DetailRow("Summary", record.summaryText)

        if (record.messages.isNotEmpty()) {
            DetailRow("Messages", record.messages.joinToString(" | "))
        }

        val priorityLabel = when (record.priority) {
            Notification.PRIORITY_MAX -> "MAX"
            Notification.PRIORITY_HIGH -> "HIGH"
            Notification.PRIORITY_LOW -> "LOW"
            Notification.PRIORITY_MIN -> "MIN"
            else -> "DEFAULT"
        }
        DetailRow("Priority", priorityLabel)

        val visibilityLabel = when (record.visibility) {
            Notification.VISIBILITY_PUBLIC -> "PUBLIC"
            Notification.VISIBILITY_PRIVATE -> "PRIVATE"
            Notification.VISIBILITY_SECRET -> "SECRET"
            else -> "PRIVATE"
        }
        DetailRow("Visibility", visibilityLabel)

        if (record.actionLabels.isNotEmpty()) {
            DetailRow("Actions", record.actionLabels.joinToString(" · "))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ComposeColor(0xFF6B7280),
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = ComposeColor(0xFF111827),
            modifier = Modifier.weight(0.65f)
        )
    }
}

private fun formatTime(timeMs: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timeMs))
}
