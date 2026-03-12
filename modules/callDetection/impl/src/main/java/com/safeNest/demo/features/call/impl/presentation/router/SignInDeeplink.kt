package com.safeNest.demo.features.call.impl.presentation.router

import android.net.Uri
import com.safeNest.demo.features.call.api.router.CallDetectionRouterConst
import com.uney.core.router.InternalRouter

object CallDetectionDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .build()
    }
}