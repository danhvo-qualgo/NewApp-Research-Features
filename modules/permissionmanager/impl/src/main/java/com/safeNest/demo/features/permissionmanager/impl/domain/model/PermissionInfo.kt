package com.safeNest.demo.features.permissionmanager.impl.domain.model

data class PermissionInfo(
    val name: String,
    val label: String,
    val isGranted: Boolean,
    val protectionLevel: PermissionProtectionLevel
)

enum class PermissionProtectionLevel {
    NORMAL, DANGEROUS, SIGNATURE, OTHER
}
