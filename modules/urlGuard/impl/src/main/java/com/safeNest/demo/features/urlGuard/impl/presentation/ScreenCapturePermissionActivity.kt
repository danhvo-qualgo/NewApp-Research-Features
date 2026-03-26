package com.safeNest.demo.features.urlGuard.impl.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.safeNest.demo.features.urlGuard.impl.presentation.ScreenCapturePermissionActivity.Companion.createIntent
import com.safeNest.demo.features.urlGuard.impl.presentation.ScreenCapturePermissionActivity.Companion.onResult

/**
 * Transparent trampoline activity whose sole job is to show the system
 * "Start recording?" dialog required for MediaProjection on API 24–29.
 *
 * On API 30+ the AccessibilityService.takeScreenshot() API is used instead,
 * so this activity is never started on those devices.
 *
 * ── Usage ─────────────────────────────────────────────────────────────────
 *   1. Assign [onResult] (companion object) before starting the activity.
 *   2. Start via [createIntent].
 *   3. The callback is invoked once on the main thread with (resultCode, data)
 *      and the activity finishes itself immediately after.
 *
 * ── Why a companion-object callback instead of a broadcast? ───────────────
 *   The callback must reach the AccessibilityService instance which is already
 *   live in-process. A static WeakReference-backed lambda avoids a round-trip
 *   through the Intent/Broadcast system while keeping the coupling minimal.
 *   The lambda is nulled out immediately after it is invoked, so there is no
 *   long-lived memory leak.
 */
class ScreenCapturePermissionActivity : ComponentActivity() {

    private var resultDelivered = false

    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "MediaProjection dialog result: code=${result.resultCode}")
        deliverResult(result.resultCode, result.data)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            // Activity was recreated (e.g. rotation) — avoid launching the dialog twice.
            finish()
            return
        }

        val projMgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projectionLauncher.launch(projMgr.createScreenCaptureIntent())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Covers the case where the user pressed Back to dismiss the dialog without
        // granting permission (launcher callback is never called in that case).
        if (!resultDelivered) {
            deliverResult(Activity.RESULT_CANCELED, null)
        }
    }

    private fun deliverResult(resultCode: Int, data: Intent?) {
        if (resultDelivered) return
        resultDelivered = true
        onResult?.invoke(resultCode, data)
        onResult = null
    }

    companion object {
        private const val TAG = "ScreenCapturePermission"

        /**
         * Set this **before** calling [createIntent].
         *
         * Invoked exactly once with `(RESULT_OK, data)` on success, or
         * `(RESULT_CANCELED, null)` if the user dismissed the dialog.
         * Automatically cleared after the first invocation.
         */
        var onResult: ((Int, Intent?) -> Unit)? = null

        fun createIntent(context: Context): Intent =
            Intent(context, ScreenCapturePermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    }
}
