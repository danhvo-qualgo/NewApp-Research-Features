package com.safeNest.demo.features.splash.impl.presentation.screen.permissions

import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

data class PermissionUiState(
    val permissionStates: Map<PermissionType, Boolean> = emptyMap(),
) {
    val allPermissionsGranted: Boolean
        get() = permissionStates.all { it.value }
}
