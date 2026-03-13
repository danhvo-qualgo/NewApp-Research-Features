package com.safeNest.demo.features.permissionManager.impl.presentation.screen

import com.safeNest.demo.features.permissionManager.impl.domain.model.AppInfo

data class PermissionManagerUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppInfo> = emptyList(),
    val showSystemApps: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    /** Apps filtered by the current search query and system-app toggle. */
    val displayedApps: List<AppInfo>
        get() = allApps.filter { app ->
            (showSystemApps || !app.isSystemApp) &&
                    (searchQuery.isBlank() ||
                            app.appName.contains(searchQuery, ignoreCase = true) ||
                            app.packageName.contains(searchQuery, ignoreCase = true))
        }
}
