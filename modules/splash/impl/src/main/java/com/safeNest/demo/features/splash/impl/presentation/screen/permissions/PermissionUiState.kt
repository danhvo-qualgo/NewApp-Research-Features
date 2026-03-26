package com.safeNest.demo.features.splash.impl.presentation.screen.permissions

import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

/**
 * Immutable snapshot of the permissions screen state.
 *
 * @param permissionStates Maps every [PermissionType] to whether it is
 *   currently granted (`true`) or not (`false`).
 */
data class PermissionUiState(
    val permissionStates: Map<PermissionType, Boolean> = emptyMap(),
    val dnsPermissionState: Boolean = false,
    val loading: Boolean = false,
    val showDownloadDialog: Boolean = false
) {
    val allPermissionsGranted: Boolean
        get() = permissionStates.all { it.value } && dnsPermissionState
}
