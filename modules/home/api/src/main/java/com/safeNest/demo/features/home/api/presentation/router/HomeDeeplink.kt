package com.safeNest.demo.features.home.api.presentation.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object HomeDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(HomeRouterConst.HOST)
            .build()
    }
}