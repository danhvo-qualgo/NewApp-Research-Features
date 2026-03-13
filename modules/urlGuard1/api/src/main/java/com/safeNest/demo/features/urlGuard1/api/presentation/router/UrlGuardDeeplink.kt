package com.safeNest.demo.features.urlGuard1.api.presentation.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object UrlGuardDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(UrlGuardRouterConst.HOST)
            .build()
    }
}