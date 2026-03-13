package com.safeNest.demo.features.splash.impl.data.handler

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import javax.inject.Inject
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

internal class MicrophonePermissionHandler @Inject constructor() : PermissionHandler {

    override val type: PermissionType = PermissionType.MICROPHONE

    override fun isGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
}
