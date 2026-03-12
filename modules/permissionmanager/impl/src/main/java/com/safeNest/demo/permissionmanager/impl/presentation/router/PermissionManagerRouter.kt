package com.safeNest.demo.permissionmanager.impl.presentation.router

import android.content.Context
import com.safeNest.demo.permissionmanager.api.presentation.router.PermissionManagerRouterConst
import com.safeNest.demo.permissionmanager.impl.presentation.PermissionManagerActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class PermissionManagerRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, PermissionManagerRouterConst.HOST, PermissionManagerActivity::class)
