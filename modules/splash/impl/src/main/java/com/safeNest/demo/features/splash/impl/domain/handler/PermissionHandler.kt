package com.safeNest.demo.features.splash.impl.domain.handler

import android.content.Context
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

interface PermissionHandler {
    val type: PermissionType
    fun isGranted(context: Context): Boolean
    fun requestPermission(context: Context) {}
}