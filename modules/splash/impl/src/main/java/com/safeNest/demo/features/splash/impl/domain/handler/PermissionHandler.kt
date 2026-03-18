package com.safeNest.demo.features.splash.impl.domain.handler

import android.content.Context
import android.content.Intent
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

interface PermissionHandler {
    val type: PermissionType
    fun isGranted(context: Context): Boolean
    fun requestPermission(context: Context) {}
    /** Returns the [Intent] to pass to [startActivityForResult] for role-based permissions, or `null` for all other types. */
    fun buildRoleRequestIntent(context: Context): Intent? = null
}