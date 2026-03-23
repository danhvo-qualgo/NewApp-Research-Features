package com.safeNest.demo.features.callProtection.impl.presentation.router

import android.net.Uri
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.router.CallDetectionRouterConst
import com.uney.core.router.InternalRouter
import kotlinx.serialization.json.Json

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

    fun entryPointMissingCall(callerIdInfo: CallerIdInfo): Uri {
        val payload = Uri.encode(Json.encodeToString(callerIdInfo))
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .appendQueryParameter(PAGE, MISSING_CALL)
            .appendQueryParameter(DATA_PAYLOAD, payload)
            .build()
    }

    fun entryPointReviewCall(callerIdInfo: CallerIdInfo): Uri {
        val payload = Uri.encode(Json.encodeToString(callerIdInfo))
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(CallDetectionRouterConst.HOST)
            .appendQueryParameter(PAGE, REVIEW_CALL)
            .appendQueryParameter(DATA_PAYLOAD, payload)
            .build()
    }

    const val BLOCKLIST = "BLOCKLIST"

    const val WHITELIST = "WHITELIST"

    const val MISSING_CALL = "MISSING_CALL"

    const val REVIEW_CALL = "REVIEW_CALL"

    const val PAGE = "PAGE"
    const val DATA_PAYLOAD = "DATA_PAYLOAD"
}