package com.safeNest.demo.features.splash.impl.domain.model

sealed interface PermissionRequestType {
    data class RunTime(val permission: String): PermissionRequestType
    data class RunTimes(val permissions: List<String>): PermissionRequestType
    data object Settings: PermissionRequestType
    /** Role must be requested via [android.app.role.RoleManager.createRequestRoleIntent] + startActivityForResult. */
    data object Role: PermissionRequestType
}