package com.safeNest.demo.features.splash.impl.presentation.screen.permissions

import android.content.Intent
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

sealed interface PermissionEvent {
    data class RequestPermissionEvent(val permissionType: PermissionType): PermissionEvent
    /** Carries a ready-to-use role-request [Intent] that must be launched via [startActivityForResult]. */
    data class RequestRoleEvent(val permissionType: PermissionType, val intent: Intent): PermissionEvent
}