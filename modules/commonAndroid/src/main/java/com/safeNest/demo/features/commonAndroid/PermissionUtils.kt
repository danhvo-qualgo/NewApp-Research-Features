package com.safeNest.demo.features.commonAndroid

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

// ─────────────────────────────────────────────────────────────────────────────
// Permission result callback
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Simple callback delivered when a runtime permission request resolves.
 *
 * @param permission  The manifest permission string that was requested.
 * @param granted     `true` if the user allowed the permission.
 * @param shouldShowRationale  `true` if the app should explain *why* it needs
 *        the permission before asking again. Always `false` when [granted] is
 *        `true` or when the user has permanently denied ("Don't ask again").
 */
data class PermissionResult(
    val permission: String,
    val granted: Boolean,
    val shouldShowRationale: Boolean,
)

// ─────────────────────────────────────────────────────────────────────────────
// SpecialPermission — string keys
// ─────────────────────────────────────────────────────────────────────────────

/**
 * String keys for every special (non-runtime) permission supported by
 * [PermissionUtils.openPermissionSettings].
 *
 * Pass one of these constants to [PermissionUtils.openPermissionSettings] (or
 * the [Context.openPermissionSettings] extension) to navigate to the
 * corresponding system Settings screen.
 *
 * ```kotlin
 * PermissionUtils.openPermissionSettings(context, SpecialPermission.OVERLAY)
 * PermissionUtils.openPermissionSettings(context, SpecialPermission.ACCESSIBILITY)
 * PermissionUtils.openPermissionSettings(context, SpecialPermission.NOTIFICATION_LISTENER)
 * PermissionUtils.openPermissionSettings(context, SpecialPermission.WRITE_SETTINGS)
 * PermissionUtils.openPermissionSettings(context, SpecialPermission.APP_SETTINGS)
 * ```
 */
object SpecialPermission {
    /** `SYSTEM_ALERT_WINDOW` – "Display over other apps" overlay permission. */
    const val OVERLAY = "overlay"

    /** System Accessibility settings – enable / disable an AccessibilityService. */
    const val ACCESSIBILITY = "accessibility"

    /** Notification listener access – enable / disable a NotificationListenerService. */
    const val NOTIFICATION_LISTENER = "notification_listener"

    /** `WRITE_SETTINGS` – "Modify system settings". */
    const val WRITE_SETTINGS = "write_settings"

    /**
     * The app's own detail page in system Settings.
     * Use this as a fallback when a runtime permission is permanently denied.
     */
    const val APP_SETTINGS = "app_settings"
}

// ─────────────────────────────────────────────────────────────────────────────
// PermissionUtils
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Central utility for every permission-related operation in the app.
 *
 * Covers three permission categories:
 *
 * 1. **Runtime permissions** – regular Manifest permissions (e.g. CAMERA,
 *    RECORD_AUDIO, POST_NOTIFICATIONS) that the user grants via the system
 *    dialog at runtime.
 *
 * 2. **Special / system permissions** – permissions that cannot be granted via
 *    the normal dialog but instead require navigating to a dedicated Settings
 *    page. Use [openPermissionSettings] with a [SpecialPermission] key:
 *    - [SpecialPermission.OVERLAY]  (`SYSTEM_ALERT_WINDOW`)
 *    - [SpecialPermission.ACCESSIBILITY]
 *    - [SpecialPermission.NOTIFICATION_LISTENER]
 *    - [SpecialPermission.WRITE_SETTINGS]
 *    - [SpecialPermission.APP_SETTINGS]
 *
 * 3. **App Settings shortcut** – opens the app's own permission page in the
 *    system Settings, useful when a permission is permanently denied.
 *
 * ### Typical usage
 *
 * ```kotlin
 * // ── Runtime ──────────────────────────────────────────────────────────────
 * if (!PermissionUtils.isRuntimePermissionGranted(context, Manifest.permission.CAMERA)) {
 *     PermissionUtils.requestRuntimePermission(activity, Manifest.permission.CAMERA, RC_CAMERA)
 * }
 *
 * // ── Special permissions (single entry point) ──────────────────────────────
 * if (!PermissionUtils.isOverlayPermissionGranted(context)) {
 *     PermissionUtils.openPermissionSettings(context, SpecialPermission.OVERLAY)
 * }
 * if (!PermissionUtils.isAccessibilityServiceEnabled(context, MyService::class.java)) {
 *     PermissionUtils.openPermissionSettings(context, SpecialPermission.ACCESSIBILITY)
 * }
 * if (!PermissionUtils.isNotificationListenerEnabled(context, MyListener::class.java)) {
 *     PermissionUtils.openPermissionSettings(context, SpecialPermission.NOTIFICATION_LISTENER)
 * }
 * ```
 */
object PermissionUtils {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Runtime permissions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns `true` if [permission] has already been granted to this app.
     *
     * Works for all API levels.
     *
     * @param context    Any valid [Context].
     * @param permission A manifest permission string, e.g.
     *                   `Manifest.permission.CAMERA`.
     */
    fun isRuntimePermissionGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Returns `true` if **all** permissions in [permissions] are already
     * granted.
     */
    fun areRuntimePermissionsGranted(
        context: Context,
        vararg permissions: String,
    ): Boolean = permissions.all { isRuntimePermissionGranted(context, it) }

    /**
     * Launches the system permission dialog for a single [permission].
     *
     * The result is delivered to [Activity.onRequestPermissionsResult] using
     * the supplied [requestCode].
     *
     * Consider using `ActivityResultContracts.RequestPermission` in Compose or
     * modern Activity code for a cleaner result API.
     */
    fun requestRuntimePermission(
        activity: Activity,
        permission: String,
        requestCode: Int,
    ) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

    /**
     * Launches the system permission dialogs for multiple [permissions] at
     * once.
     *
     * Results are delivered to [Activity.onRequestPermissionsResult].
     */
    fun requestRuntimePermissions(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int,
    ) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    /**
     * Returns `true` if the system recommends showing a rationale UI before
     * re-requesting [permission].
     *
     * This is `true` when:
     * - The user has previously denied the permission **at least once**, AND
     * - The user has **not** checked "Don't ask again".
     *
     * It is always `false` when the permission is already granted or permanently
     * denied.
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    /**
     * Builds a [PermissionResult] from the arrays returned by
     * [Activity.onRequestPermissionsResult].
     *
     * ```kotlin
     * override fun onRequestPermissionsResult(
     *     requestCode: Int,
     *     permissions: Array<String>,
     *     grantResults: IntArray,
     * ) {
     *     super.onRequestPermissionsResult(requestCode, permissions, grantResults)
     *     val results = PermissionUtils.parsePermissionResults(this, permissions, grantResults)
     *     results.forEach { result ->
     *         if (result.granted) { /* proceed */ }
     *         else if (result.shouldShowRationale) { /* show rationale */ }
     *         else { /* permanently denied — open app settings */ }
     *     }
     * }
     * ```
     */
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

    /**
     * Returns `true` if the "Display over other apps" (Overlay) permission is
     * currently granted to this app.
     *
     * Always `true` on API < 23 where the permission did not exist.
     */
    fun isOverlayPermissionGranted(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    // ─────────────────────────────────────────────────────────────────────────
    // 2b. Accessibility Service
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns `true` if the [AccessibilityService][android.accessibilityservice.AccessibilityService]
     * identified by [serviceClass] is currently enabled.
     *
     * @param context      Any valid [Context].
     * @param serviceClass The concrete class of the accessibility service to
     *                     check, e.g. `MyAccessibilityService::class.java`.
     */
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

    /**
     * Returns `true` if a [NotificationListenerService][android.service.notification.NotificationListenerService]
     * identified by [serviceClass] is currently enabled for this app.
     *
     * @param context      Any valid [Context].
     * @param serviceClass The concrete class of the notification listener service,
     *                     e.g. `MyNotificationListenerService::class.java`.
     */
    fun isNotificationListenerEnabled(
        context: Context,
        serviceClass: Class<*>,
    ): Boolean = isNotificationListenerEnabled(context, ComponentName(context, serviceClass))

    /**
     * Returns `true` if the notification listener identified by
     * [componentName] is currently enabled.
     */
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
    // 3. Write Settings  (WRITE_SETTINGS)
    // ─────────────────────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Unified special-permission settings opener
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Opens the system Settings screen that corresponds to the [permission] key.
     *
     * Pass one of the [SpecialPermission] string constants:
     *
     * | [permission] value                        | Settings screen opened                        |
     * |-------------------------------------------|-----------------------------------------------|
     * | [SpecialPermission.OVERLAY]               | "Display over other apps" for this app        |
     * | [SpecialPermission.ACCESSIBILITY]         | Accessibility services list                   |
     * | [SpecialPermission.NOTIFICATION_LISTENER] | Notification access list                      |
     * | [SpecialPermission.WRITE_SETTINGS]        | "Modify system settings" for this app         |
     * | [SpecialPermission.APP_SETTINGS]          | App detail page (runtime-permission fallback) |
     *
     * Unknown values are silently ignored.
     *
     * After the user returns from Settings, re-check the relevant
     * `is*Granted` / `is*Enabled` function in `onResume` or an
     * `ActivityResult` callback.
     *
     * ```kotlin
     * PermissionUtils.openPermissionSettings(context, SpecialPermission.OVERLAY)
     * PermissionUtils.openPermissionSettings(context, SpecialPermission.ACCESSIBILITY)
     * PermissionUtils.openPermissionSettings(context, SpecialPermission.NOTIFICATION_LISTENER)
     * ```
     */
    fun openPermissionSettings(context: Context, permission: String) {
        buildPermissionSettingsIntent(context, permission)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?.let(context::startActivity)
    }

    /**
     * Builds and returns the [Intent] that [openPermissionSettings] would
     * launch for the given [permission] key, **without** starting it.
     *
     * Useful in Compose when you need to hand the intent directly to a
     * `StartActivityForResult` launcher — for example, the overlay permission
     * where you want to re-check state the moment the user returns.
     *
     * Returns `null` for unknown [permission] keys.
     *
     * ```kotlin
     * val intent = PermissionUtils.buildPermissionSettingsIntent(context, SpecialPermission.OVERLAY)
     * overlayLauncher.launch(intent)
     * ```
     */
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
            SpecialPermission.APP_SETTINGS -> Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
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
 * @see SpecialPermission
 */
fun Context.openPermissionSettings(permission: String): Unit =
    PermissionUtils.openPermissionSettings(this, permission)

/** @see PermissionUtils.shouldShowRationale */
fun Activity.shouldShowPermissionRationale(permission: String): Boolean =
    PermissionUtils.shouldShowRationale(this, permission)
