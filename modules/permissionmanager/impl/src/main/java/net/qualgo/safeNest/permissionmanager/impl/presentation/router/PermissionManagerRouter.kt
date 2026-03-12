package net.qualgo.safeNest.permissionmanager.impl.presentation.router

import android.content.Context
import net.qualgo.safeNest.permissionmanager.api.presentation.router.PermissionManagerRouterConst
import net.qualgo.safeNest.permissionmanager.impl.presentation.PermissionManagerActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class PermissionManagerRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
): InternalRouter(context, PermissionManagerRouterConst.HOST, PermissionManagerActivity::class)
