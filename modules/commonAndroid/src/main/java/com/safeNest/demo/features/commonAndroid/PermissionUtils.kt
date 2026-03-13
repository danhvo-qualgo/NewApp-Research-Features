package com.safeNest.demo.features.commonAndroid

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

// ─────────────────────────────────────────────────────────────────────────────
// Permission result callback
// ─────────────────────────────────────────────────────────────────────────────

data class PermissionResult(
    val permission: String,
    val granted: Boolean,
    val shouldShowRationale: Boolean,
)

// ─────────────────────────────────────────────────────────────────────────────
// SpecialPermission — string keys
// ─────────────────────────────────────────────────────────────────────────────


object SpecialPermission {
    /** `SYSTEM_ALERT_WINDOW` – "Display over other apps" overlay permission. */
    const val OVERLAY = "overlay"

    /** System Accessibility settings – enable / disable an AccessibilityService. */
    const val ACCESSIBILITY = "accessibility"

    /** Notification listener access – enable / disable a NotificationListenerService. */
    const val NOTIFICATION_LISTENER = "notification_listener"

    /** `WRITE_SETTINGS` – "Modify system settings". */
    const val WRITE_SETTINGS = "write_settings"
}

// ─────────────────────────────────────────────────────────────────────────────
// PermissionUtils
// ─────────────────────────────────────────────────────────────────────────────

object PermissionUtils {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Runtime permissions
    // ─────────────────────────────────────────────────────────────────────────

    fun isRuntimePermissionGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRationale(activity: Activity, permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    fun parsePermissionResults(
        activity: Activity,
        permissions: Array<out String>,
        grantResults: IntArray,
    ): List<PermissionResult> = permissions.mapIndexed { index, permission ->
        val granted = grantResults.getOrElse(index) { PackageManager.PERMISSION_DENIED } ==
                PackageManager.PERMISSION_GRANTED
        PermissionResult(
            permission = permission,
            granted = granted,
            shouldShowRationale = !granted && shouldShowRationale(activity, permission),
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2a. Overlay permission  (SYSTEM_ALERT_WINDOW)
    // ─────────────────────────────────────────────────────────────────────────

    fun isOverlayPermissionGranted(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    // ─────────────────────────────────────────────────────────────────────────
    // 2b. Accessibility Service
    // ─────────────────────────────────────────────────────────────────────────

    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<*>,
    ): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        val targetComponent = ComponentName(context, serviceClass)
        return enabledServices.any { info ->
            info.resolveInfo?.serviceInfo?.let { si ->
                ComponentName(si.packageName, si.name) == targetComponent
            } == true
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2c. Notification Listener Service
    // ─────────────────────────────────────────────────────────────────────────

    fun isNotificationListenerEnabled(
        context: Context,
        serviceClass: Class<*>,
    ): Boolean = isNotificationListenerEnabled(context, ComponentName(context, serviceClass))


    fun isNotificationListenerEnabled(
        context: Context,
        componentName: ComponentName,
    ): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return flat.split(':').any { entry ->
            runCatching { ComponentName.unflattenFromString(entry) == componentName }
                .getOrDefault(false)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Unified special-permission settings opener
    // ─────────────────────────────────────────────────────────────────────────
    fun openPermissionSettings(context: Context, permission: String) {
        buildPermissionSettingsIntent(context, permission)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?.let(context::startActivity)
    }

    fun buildPermissionSettingsIntent(context: Context, permission: String): Intent? =
        when (permission) {
            SpecialPermission.OVERLAY -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri(),
            )
            SpecialPermission.ACCESSIBILITY -> Intent(
                Settings.ACTION_ACCESSIBILITY_SETTINGS,
            )
            SpecialPermission.NOTIFICATION_LISTENER -> Intent(
                Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS,
            )
            SpecialPermission.WRITE_SETTINGS -> Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                "package:${context.packageName}".toUri(),
            )
            else -> null
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// Context / Activity extension functions (syntactic sugar)
// ─────────────────────────────────────────────────────────────────────────────

/** @see PermissionUtils.isRuntimePermissionGranted */
fun Context.isPermissionGranted(permission: String): Boolean =
    PermissionUtils.isRuntimePermissionGranted(this, permission)

/** @see PermissionUtils.isOverlayPermissionGranted */
fun Context.isOverlayPermissionGranted(): Boolean =
    PermissionUtils.isOverlayPermissionGranted(this)

/** @see PermissionUtils.isAccessibilityServiceEnabled */
fun Context.isAccessibilityServiceEnabled(serviceClass: Class<*>): Boolean =
    PermissionUtils.isAccessibilityServiceEnabled(this, serviceClass)

/** @see PermissionUtils.isNotificationListenerEnabled */
fun Context.isNotificationListenerEnabled(serviceClass: Class<*>): Boolean =
    PermissionUtils.isNotificationListenerEnabled(this, serviceClass)

/**
 * Opens the system Settings screen for [permission].
 *
 * @see PermissionUtils.openPermissionSettings
 * @see SpeciarelPermission
 */
fun Context.openPermissionSettings(permission: String): Unit =
    PermissionUtils.openPermissionSettings(this, permission)

/** @see PermissionUtils.shouldShowRationale */
fun Activity.shouldShowPermissionRationale(permission: String): Boolean =
    PermissionUtils.shouldShowRationale(this, permission)
