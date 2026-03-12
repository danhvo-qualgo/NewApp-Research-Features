package com.safeNest.demo.features.scamAnalyzer.api.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object ScamAnalyzerDeepLink {
    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(ScamAnalyzerRouterConst.HOST)
            .build()
    }
}

object ScamAnalyzerRouterConst {
    const val HOST = "featuresScamAnalyzer"
}