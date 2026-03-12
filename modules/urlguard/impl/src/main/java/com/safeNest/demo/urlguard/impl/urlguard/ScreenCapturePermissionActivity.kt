package com.safeNest.demo.urlguard.impl.urlguard

import android.app.Activity
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Transparent trampoline activity whose sole job is to show the system
 * "Start recording?" consent dialog and relay the result back to
 * [UrlGuardAccessibilityService].
 *
 * It is launched by the service via [startActivity] (FLAG_ACTIVITY_NEW_TASK),
 * fires the consent dialog immediately on create, and finishes itself as soon
 * as the user accepts or cancels — leaving no visible UI behind.
 */
class ScreenCapturePermissionActivity : ComponentActivity() {

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val service = UrlGuardAccessibilityService.getInstance()
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            service?.onProjectionReady(result.resultCode, result.data!!)
        } else {
            service?.onProjectionDenied()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projMgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(projMgr.createScreenCaptureIntent())
    }
}
