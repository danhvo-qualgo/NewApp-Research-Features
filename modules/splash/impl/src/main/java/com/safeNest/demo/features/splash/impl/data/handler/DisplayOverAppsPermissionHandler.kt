package com.safeNest.demo.features.splash.impl.data.handler

import android.content.Context
import javax.inject.Inject
import com.safeNest.demo.features.commonAndroid.SpecialPermission
import com.safeNest.demo.features.commonAndroid.isOverlayPermissionGranted
import com.safeNest.demo.features.commonAndroid.openPermissionSettings
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

internal class DisplayOverAppsPermissionHandler @Inject constructor() : PermissionHandler {

    override val type: PermissionType = PermissionType.DISPLAY_OVER_APPS

    override fun isGranted(context: Context): Boolean = context.isOverlayPermissionGranted()

    override fun requestPermission(context: Context) {
        context.openPermissionSettings(SpecialPermission.OVERLAY)
    }

}
