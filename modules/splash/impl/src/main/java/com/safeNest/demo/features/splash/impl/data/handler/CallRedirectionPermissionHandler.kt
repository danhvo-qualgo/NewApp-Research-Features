package com.safeNest.demo.features.splash.impl.data.handler

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import com.safeNest.demo.features.commonAndroid.createRequestRoleIntent
import com.safeNest.demo.features.commonAndroid.isRoleAvailable
import com.safeNest.demo.features.commonAndroid.isRoleHeld
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import javax.inject.Inject

// Requires ROLE_CALL_REDIRECTION (API 29+); checks availability before isRoleHeld.
internal class CallRedirectionPermissionHandler @Inject constructor() : PermissionHandler {

    override val type: PermissionType = PermissionType.CALL_REDIRECTION

    override fun isGranted(context: Context): Boolean = context.isRoleAvailable(RoleManager.ROLE_CALL_REDIRECTION) &&
        context.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)

    override fun buildRoleRequestIntent(context: Context): Intent? =
        context.createRequestRoleIntent(RoleManager.ROLE_CALL_REDIRECTION)
}
