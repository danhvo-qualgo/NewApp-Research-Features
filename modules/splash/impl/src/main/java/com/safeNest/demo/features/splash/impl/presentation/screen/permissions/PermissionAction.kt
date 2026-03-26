package com.safeNest.demo.features.splash.impl.presentation.screen.permissions

import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

sealed interface PermissionAction {
    /** User tapped the toggle for [type]. */
    data class TogglePermission(val type: PermissionType) : PermissionAction
    data class UpdatePermissionGrantedState(val type: PermissionType, val isGranted: Boolean): PermissionAction

    object ToggleDsnPermission: PermissionAction
    object ClickDownloadCa: PermissionAction
}
