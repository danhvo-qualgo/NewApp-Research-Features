package com.safeNest.demo.features.splash.impl.domain

import android.content.Context
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PermissionManager(
    private val context: Context,
    private val handlers: Set<PermissionHandler>
) {
    fun isGranted(permissionType: PermissionType): Boolean {
        val handler = handlers.find { it.type == permissionType } ?: error("Handler not found for permission type: $permissionType")
        return handler.isGranted(context)
    }

    fun requestPermission(permissionType: PermissionType) {
        val handler = handlers.find { it.type == permissionType } ?: error("Handler not found for permission type: $permissionType")
        handler.requestPermission(context)
    }

    fun isAllPermissionGrantedFlow(): Flow<Boolean> = flow {
        emit(handlers.all { it.isGranted(context) })
    }

    fun isAllPermissionGranted(): Boolean = handlers.all { it.isGranted(context) }
 }