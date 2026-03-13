package com.safeNest.demo.features.commonAndroid

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

// ─────────────────────────────────────────────────────────────────────────────
// 1. Single runtime permission
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Remembers an [ActivityResultLauncher][androidx.activity.result.ActivityResultLauncher]
 * that requests a **single** runtime permission and delivers the boolean result
 * to [onResult].
 *
 * ### Usage
 * ```kotlin
 * val launcher = rememberRuntimePermissionLauncher { granted ->
 *     if (granted) { /* proceed */ } else { /* show rationale or open settings */ }
 * }
 *
 * Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
 *     Text("Grant camera")
 * }
 * ```
 *
 * @param onResult Called on the main thread with `true` when the user grants
 *                 the permission, `false` otherwise.
 */
@Composable
fun rememberRuntimePermissionLauncher(
    onResult: (granted: Boolean) -> Unit,
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = onResult,
)

// ─────────────────────────────────────────────────────────────────────────────
// 2. Multiple runtime permissions
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Remembers an [ActivityResultLauncher][androidx.activity.result.ActivityResultLauncher]
 * that requests **multiple** runtime permissions at once and delivers a
 * `Map<permission, granted>` result to [onResult].
 *
 * ### Usage
 * ```kotlin
 * val launcher = rememberMultiplePermissionsLauncher { results ->
 *     val allGranted = results.values.all { it }
 * }
 *
 * Button(onClick = {
 *     launcher.launch(arrayOf(
 *         Manifest.permission.CAMERA,
 *         Manifest.permission.RECORD_AUDIO,
 *     ))
 * }) { Text("Grant all") }
 * ```
 *
 * @param onResult Called on the main thread with a map from each permission
 *                 string to a boolean indicating whether it was granted.
 */
@Composable
fun rememberMultiplePermissionsLauncher(
    onResult: (results: Map<String, Boolean>) -> Unit,
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = onResult,
)

// ─────────────────────────────────────────────────────────────────────────────
// 3. RuntimePermissionState — stateful single-permission helper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Stateful holder for a single runtime [permission].
 *
 * Holds whether the permission is currently [isGranted] and exposes a
 * [launch] function that opens the system permission dialog.
 *
 * Obtain an instance via [rememberRuntimePermissionState].
 */
class RuntimePermissionState internal constructor(
    /** The manifest permission string this state tracks. */
    val permission: String,
    isGranted: Boolean,
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>,
) {
    private val _isGranted = mutableStateOf(isGranted)

    /** `true` when [permission] is currently granted. */
    val isGranted: Boolean get() = _isGranted.value

    /**
     * Updates the cached granted state. Called internally when the user
     * returns from the permission dialog or from Settings.
     */
    internal fun updateGranted(value: Boolean) {
        _isGranted.value = value
    }

    /**
     * Opens the system permission dialog for [permission].
     *
     * After the dialog closes the state is automatically updated and
     * [onResult] (supplied to [rememberRuntimePermissionState]) is invoked.
     */
    fun launch() = launcher.launch(permission)
}

/**
 * Creates and remembers a [RuntimePermissionState] for the given
 * [permission].
 *
 * The granted state is kept in sync:
 * - Immediately when the permission dialog closes.
 * - Every time the composable re-enters the **RESUMED** lifecycle state
 *   (e.g. after the user navigates back from system Settings).
 *
 * ### Usage
 * ```kotlin
 * val cameraState = rememberRuntimePermissionState(Manifest.permission.CAMERA)
 *
 * if (cameraState.isGranted) {
 *     CameraPreview()
 * } else {
 *     Button(onClick = { cameraState.launch() }) { Text("Allow camera") }
 * }
 * ```
 *
 * @param permission  A manifest permission string.
 * @param onResult    Optional callback invoked with the granted boolean
 *                    immediately after the dialog closes.
 */
@Composable
fun rememberRuntimePermissionState(
    permission: String,
    onResult: (granted: Boolean) -> Unit = {},
): RuntimePermissionState {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onResult(granted)
    }

    val state = remember(permission) {
        RuntimePermissionState(
            permission = permission,
            isGranted = PermissionUtils.isRuntimePermissionGranted(context, permission),
            launcher = launcher,
        )
    }

    // Re-sync whenever the screen comes back to foreground (user may have
    // visited system Settings and granted/revoked the permission there).
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.LaunchedEffect(lifecycleOwner, permission) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            state.updateGranted(PermissionUtils.isRuntimePermissionGranted(context, permission))
        }
    }

    return state
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. MultipleRuntimePermissionsState — stateful multi-permission helper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Stateful holder for a set of runtime [permissions].
 *
 * Obtain an instance via [rememberMultipleRuntimePermissionsState].
 */
class MultipleRuntimePermissionsState internal constructor(
    /** The manifest permission strings this state tracks. */
    val permissions: List<String>,
    initialGranted: Map<String, Boolean>,
    private val launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
) {
    private val _grantedMap = mutableStateOf(initialGranted)

    /** A snapshot map of `permission → isGranted` for every tracked permission. */
    val grantedMap: Map<String, Boolean> get() = _grantedMap.value

    /** `true` when **every** tracked permission is granted. */
    val allGranted: Boolean get() = _grantedMap.value.values.all { it }

    /** Returns `true` when [permission] is currently granted. */
    fun isGranted(permission: String): Boolean = _grantedMap.value[permission] == true

    /** Updates the cached map. Called internally. */
    internal fun updateGranted(map: Map<String, Boolean>) {
        _grantedMap.value = map
    }

    /**
     * Opens the system permission dialogs for all tracked [permissions].
     *
     * Only ungranted permissions are requested; already-granted ones are
     * skipped automatically by the OS.
     */
    fun launch() = launcher.launch(permissions.toTypedArray())
}

/**
 * Creates and remembers a [MultipleRuntimePermissionsState] for the given
 * list of [permissions].
 *
 * The granted state is kept in sync:
 * - Immediately when all permission dialogs close.
 * - Every time the composable re-enters the **RESUMED** lifecycle state.
 *
 * ### Usage
 * ```kotlin
 * val state = rememberMultipleRuntimePermissionsState(
 *     listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
 * )
 *
 * if (state.allGranted) {
 *     RecordScreen()
 * } else {
 *     Button(onClick = { state.launch() }) { Text("Grant permissions") }
 * }
 * ```
 *
 * @param permissions List of manifest permission strings.
 * @param onResult    Optional callback invoked with the full result map
 *                    immediately after all dialogs close.
 */
@Composable
fun rememberMultipleRuntimePermissionsState(
    permissions: List<String>,
    onResult: (results: Map<String, Boolean>) -> Unit = {},
): MultipleRuntimePermissionsState {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        onResult(results)
    }

    fun currentGrantedMap() = permissions.associateWith {
        PermissionUtils.isRuntimePermissionGranted(context, it)
    }

    val state = remember(permissions) {
        MultipleRuntimePermissionsState(
            permissions = permissions,
            initialGranted = currentGrantedMap(),
            launcher = launcher,
        )
    }

    // Re-sync on every resume (user may have toggled a permission in Settings).
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.LaunchedEffect(lifecycleOwner, permissions) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            state.updateGranted(currentGrantedMap())
        }
    }

    return state
}

