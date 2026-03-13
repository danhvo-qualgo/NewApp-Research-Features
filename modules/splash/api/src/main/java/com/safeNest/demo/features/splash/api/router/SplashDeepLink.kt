package com.safeNest.demo.features.splash.api.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object SplashDeepLink {
    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(SplashRouterConst.HOST)
            .build()
    }
}

object SplashRouterConst {
    const val HOST = "featuresSplash"
}