package com.safeNest.demo.features.scamAnalyzer.api.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object ScamAnalyzerDeepLink {
    private const val PARAM_RESULT_KEY = "resultKey"
    
    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(ScamAnalyzerRouterConst.HOST)
            .build()
    }
    
    fun entryPointWithResult(resultKey: String): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(ScamAnalyzerRouterConst.HOST)
            .appendQueryParameter(PARAM_RESULT_KEY, resultKey)
            .build()
    }
}

object ScamAnalyzerRouterConst {
    const val HOST = "featuresScamAnalyzer"
    const val PARAM_RESULT_KEY = "resultKey"
}