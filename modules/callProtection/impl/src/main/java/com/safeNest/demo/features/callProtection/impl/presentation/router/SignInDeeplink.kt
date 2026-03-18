package com.safeNest.demo.features.callProtection.impl.presentation.router

import android.net.Uri
import com.safeNest.demo.features.callProtection.api.router.CallDetectionRouterConst
import com.uney.core.router.InternalRouter

object CallDetectionDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .build()
    }
    fun entryPointBlocklist(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .appendQueryParameter(PAGE, BLOCKLIST)
            .build()
    }
    fun entryPointWhitelist(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .appendQueryParameter(PAGE, WHITELIST)
            .build()
    }

    const val BLOCKLIST = "BLOCKLIST"

    const val WHITELIST = "WHITELIST"

    const val PAGE = "PAGE"
}