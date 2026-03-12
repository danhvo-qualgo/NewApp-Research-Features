package com.safeNest.demo.features.call.api.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object CallDetectionDeepLink {
    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .build()
    }
}

object CallDetectionRouterConst {
    const val HOST = "callCallDetection"
}