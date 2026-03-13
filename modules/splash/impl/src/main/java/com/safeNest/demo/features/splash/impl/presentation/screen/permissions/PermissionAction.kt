package net.qualgo.safeNest.onboarding.impl.permission.presentation

import net.qualgo.safeNest.onboarding.api.permission.PermissionType

sealed interface PermissionAction {
    /** User tapped the toggle for [type]. */
    data class TogglePermission(val type: PermissionType) : PermissionAction
}
