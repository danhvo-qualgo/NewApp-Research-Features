package net.qualgo.safeNest.onboarding.impl.permission.presentation

import com.safeNest.demo.features.splash.impl.presentation.screen.permissions.PermissionType

sealed interface PermissionAction {
    /** User tapped the toggle for [type]. */
    data class TogglePermission(val type: PermissionType) : PermissionAction
}
