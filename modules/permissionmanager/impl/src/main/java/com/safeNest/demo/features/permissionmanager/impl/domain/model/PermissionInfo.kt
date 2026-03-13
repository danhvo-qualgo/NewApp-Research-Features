package com.safeNest.demo.features.permissionManager.impl.domain.model

data class PermissionInfo(
    val name: String,
    val label: String,
    val isGranted: Boolean,
    val protectionLevel: PermissionProtectionLevel
)

enum class PermissionProtectionLevel {
    NORMAL, DANGEROUS, SIGNATURE, OTHER
}
