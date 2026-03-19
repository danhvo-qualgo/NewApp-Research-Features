package com.safeNest.demo.features.permissionManager.impl.domain.usecase

import com.safeNest.demo.features.permissionManager.api.domain.GetAppPermissionInfoUseCase
import com.safeNest.demo.features.permissionManager.api.domain.model.PermissionInfo
import com.safeNest.demo.features.permissionManager.impl.domain.AppPermissionManager
import javax.inject.Inject

class GetAppPermissionInfoUseCaseImpl @Inject constructor(
    private val permissionManager: AppPermissionManager
): GetAppPermissionInfoUseCase {
    override suspend operator fun invoke(packageName: String): List<PermissionInfo> {
        return permissionManager.getPermissionsForPackage(packageName)
    }
}