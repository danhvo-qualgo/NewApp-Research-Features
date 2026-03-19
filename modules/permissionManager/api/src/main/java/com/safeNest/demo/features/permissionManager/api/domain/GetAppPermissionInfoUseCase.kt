package com.safeNest.demo.features.permissionManager.api.domain

import com.safeNest.demo.features.permissionManager.api.domain.model.PermissionInfo

interface GetAppPermissionInfoUseCase {
    suspend operator fun invoke(packageName: String): List<PermissionInfo>
}
