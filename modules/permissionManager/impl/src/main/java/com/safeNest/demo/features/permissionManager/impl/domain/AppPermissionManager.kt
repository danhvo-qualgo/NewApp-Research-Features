package com.safeNest.demo.features.permissionManager.impl.domain

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.safeNest.demo.features.permissionManager.api.domain.model.AppCategory
import com.safeNest.demo.features.permissionManager.api.domain.model.AppInfo
import com.safeNest.demo.features.permissionManager.api.domain.model.InstallSource
import com.safeNest.demo.features.permissionManager.api.domain.model.PermissionInfo
import com.safeNest.demo.features.permissionManager.api.domain.model.PermissionProtectionLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Queries the [PackageManager] to enumerate all installed applications together with
 * every permission each app declares (via <uses-permission>) and whether that permission
 * has actually been granted to the app at runtime.
 */
@Singleton
class AppPermissionManager @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {

    /**
     * Returns a list of [AppInfo] objects sorted alphabetically by app name.
     * Each [AppInfo] contains the full list of permissions the app requested and
     * whether each one is currently granted.
     *
     * Must be called off the main thread; suspends on [Dispatchers.IO].
     */
    @Suppress("DEPRECATION")
    suspend fun getPermissionsForPackage(packageName: String): List<PermissionInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val pkgInfo = runCatching {
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        }.getOrNull() ?: return@withContext emptyList()
        buildAppInfo(pm, pkgInfo).permissions
    }

    @Suppress("DEPRECATION")
    suspend fun getInstalledAppsWithPermissions(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager

        val packages: List<PackageInfo> = runCatching {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }.getOrElse { emptyList() }
        packages.mapNotNull { pkgInfo ->
            runCatching { buildAppInfo(pm, pkgInfo) }.getOrNull()
        }.sortedBy { it.appName.lowercase() }
    }

    @Suppress("DEPRECATION")
    private fun buildAppInfo(pm: PackageManager, pkgInfo: PackageInfo): AppInfo {
        val appName = pkgInfo.applicationInfo
            ?.let { pm.getApplicationLabel(it).toString() }
            ?: pkgInfo.packageName
        Log.d("xxx", "signature size: ${pkgInfo.signatures?.size}")
        val isSystemApp = pkgInfo.applicationInfo?.flags
            ?.let { (it and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 }
            ?: false

        val requestedPerms: Array<String> = pkgInfo.requestedPermissions ?: emptyArray()
        val requestedFlags: IntArray = pkgInfo.requestedPermissionsFlags ?: IntArray(0)

        val permissions = requestedPerms.mapIndexed { idx, permName ->
            val isGranted = if (idx < requestedFlags.size) {
                (requestedFlags[idx] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            } else {
                pm.checkPermission(
                    permName,
                    pkgInfo.packageName
                ) == PackageManager.PERMISSION_GRANTED
            }

            val permInfo = runCatching { pm.getPermissionInfo(permName, 0) }.getOrNull()
            val protection = permInfo?.let {
                @Suppress("DEPRECATION")
                when (it.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_MASK_BASE) {
                    android.content.pm.PermissionInfo.PROTECTION_DANGEROUS -> PermissionProtectionLevel.DANGEROUS
                    android.content.pm.PermissionInfo.PROTECTION_NORMAL -> PermissionProtectionLevel.NORMAL
                    android.content.pm.PermissionInfo.PROTECTION_SIGNATURE -> PermissionProtectionLevel.SIGNATURE
                    else -> PermissionProtectionLevel.OTHER
                }
            } ?: PermissionProtectionLevel.OTHER

            val label = permName.substringAfterLast('.')
                .replace('_', ' ')
                .lowercase()
                .split(' ')
                .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }

            PermissionInfo(
                name = permName,
                label = label,
                isGranted = isGranted,
                protectionLevel = protection
            )
        }

        val installSource = resolveInstallSource(pm, pkgInfo.packageName, isSystemApp)
        val category = resolveCategory(pkgInfo)

        return AppInfo(
            appName = appName,
            packageName = pkgInfo.packageName,
            isSystemApp = isSystemApp,
            category = category,
            installSource = installSource,
            permissions = permissions
        )
    }

    /**
     * Reads [ApplicationInfo.category] (API 26+) from the package and converts it to
     * a typed [AppCategory].  Returns [AppCategory.UNDEFINED] on older APIs or when the
     * field is absent.
     */
    private fun resolveCategory(pkgInfo: PackageInfo): AppCategory {
        val rawCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pkgInfo.applicationInfo?.category ?: ApplicationInfo.CATEGORY_UNDEFINED
        } else {
            ApplicationInfo.CATEGORY_UNDEFINED
        }
        return AppCategory.fromRaw(rawCategory)
    }

    @Suppress("DEPRECATION")
    private fun resolveInstallSource(
        pm: PackageManager,
        packageName: String,
        isSystemApp: Boolean
    ): InstallSource {
        val installerPackageName: String? = runCatching {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+: InstallSourceInfo carries initiating + originating package names
                val info = pm.getInstallSourceInfo(packageName)
                // Prefer the initiating package (the one that started the install session)
                info.initiatingPackageName ?: info.installingPackageName
            } else {
                pm.getInstallerPackageName(packageName)
            }
        }.getOrNull()
        if (installerPackageName != null) {
            Log.d("xxx", "app packageName: ${packageName}")
            Log.d("xxx", "install packageName: $installerPackageName")
        }

        return InstallSource.from(installerPackageName, isSystemApp)
    }
}
