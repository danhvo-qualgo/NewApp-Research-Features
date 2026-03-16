package com.safeNest.demo.features.splash.impl.data.handler

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import javax.inject.Inject


internal class PhoneAndContactsPermissionHandler @Inject constructor() : PermissionHandler {

    override val type: PermissionType = PermissionType.PHONE_AND_CONTACTS

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
    )

    override fun isGranted(context: Context): Boolean =
        requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

    companion object {
        const val REQUEST_CODE = 1001
    }
}
