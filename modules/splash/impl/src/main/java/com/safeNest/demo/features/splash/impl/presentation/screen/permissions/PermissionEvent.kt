package com.safeNest.demo.features.splash.impl.presentation.screen.permissions

import com.safeNest.demo.features.splash.impl.domain.model.PermissionType

sealed interface PermissionEvent {
    data class RequestPermissionEvent(val permissionType: PermissionType): PermissionEvent
}