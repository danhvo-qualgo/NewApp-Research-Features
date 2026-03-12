package com.safeNest.demo.permissionmanager.api.presentation.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object PermissionManagerDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(PermissionManagerRouterConst.HOST)
            .build()
    }
}
