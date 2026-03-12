package com.safeNest.demo.notificationInterceptor.api.presentation.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object NotificationInterceptorDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(NotificationInterceptorRouterConst.HOST)
            .build()
    }
}