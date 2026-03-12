package com.safeNest.demo.phishingDetection.api.presentation.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object PhishingDetectionDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(PhishingDetectionRouterConst.HOST)
            .build()
    }
}