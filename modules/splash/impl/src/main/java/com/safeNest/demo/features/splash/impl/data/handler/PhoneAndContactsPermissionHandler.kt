package com.safeNest.demo.features.splash.impl.data.handler

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionRequestType
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import javax.inject.Inject


internal class PhoneAndContactsPermissionHandler @Inject constructor() : PermissionHandler {

    override val type: PermissionType = PermissionType.PHONE_AND_CONTACTS


    override fun isGranted(context: Context): Boolean  =
        (type.requestType as PermissionRequestType.RunTimes).permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }


    companion object {
        const val REQUEST_CODE = 1001
    }
}
