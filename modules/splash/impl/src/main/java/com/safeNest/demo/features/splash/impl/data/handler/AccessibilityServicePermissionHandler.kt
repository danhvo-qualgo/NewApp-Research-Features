package com.safeNest.demo.features.splash.impl.data.handler

import android.content.Context
import com.safeNest.demo.features.commonAndroid.SpecialPermission
import com.safeNest.demo.features.commonAndroid.isAccessibilityServiceEnabled
import com.safeNest.demo.features.commonAndroid.openPermissionSettings
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import com.safeNest.demo.features.urlguard.api.UrlGuardProvider
import jakarta.inject.Inject

class AccessibilityServicePermissionHandler @Inject constructor(
    private val urlGuardProvider: UrlGuardProvider
): PermissionHandler {

    override val type: PermissionType = PermissionType.ACCESSIBILITY

    override fun isGranted(context: Context): Boolean {

        return context.isAccessibilityServiceEnabled(urlGuardProvider.getA11yServiceName())
    }

    override fun requestPermission(context: Context) {
        context.openPermissionSettings(SpecialPermission.ACCESSIBILITY)
    }
}