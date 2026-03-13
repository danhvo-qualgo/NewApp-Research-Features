package com.safeNest.demo.features.safeBrowsing.api.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object SafeBrowsingDeepLink {
    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(SafeBrowsingRouterConst.HOST)
            .build()
    }
}

object SafeBrowsingRouterConst {
    const val HOST = "featuresSafeBrowsing"
}