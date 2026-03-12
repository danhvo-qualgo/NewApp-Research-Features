package com.safeNest.demo.call.main.impl.presentation.router

import android.net.Uri
import com.safeNest.demo.call.main.api.router.CallDetectionRouterConst
import com.uney.core.router.InternalRouter

object CallDetectionDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .build()
    }
}