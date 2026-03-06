package net.qualgo.safeNest.permissionmanager.impl.domain.model

data class AppInfo(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val category: AppCategory,
    val installSource: InstallSource,
    val permissions: List<PermissionInfo>
) {
    val grantedCount: Int get() = permissions.count { it.isGranted }
    val deniedCount: Int get() = permissions.count { !it.isGranted }
    val dangerousCount: Int get() = permissions.count { it.protectionLevel == PermissionProtectionLevel.DANGEROUS }
}
