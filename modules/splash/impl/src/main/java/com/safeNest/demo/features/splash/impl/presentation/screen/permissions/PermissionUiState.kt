package net.qualgo.safeNest.onboarding.impl.permission.presentation

import com.safeNest.demo.features.splash.impl.presentation.screen.permissions.PermissionType

/**
 * Immutable snapshot of the permissions screen state.
 *
 * @param permissionStates Maps every [PermissionType] to whether it is
 *   currently granted (`true`) or not (`false`).
 */
data class PermissionUiState(
    val permissionStates: Map<PermissionType, Boolean> = emptyMap(),
)
