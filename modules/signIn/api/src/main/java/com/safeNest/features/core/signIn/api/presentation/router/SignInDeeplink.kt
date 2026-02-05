package com.safeNest.features.core.signIn.api.presentation.router

import android.net.Uri
import com.uney.core.router.InternalRouter

object SignInDeeplink {

    fun entryPoint(): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(SignInRouterConst.HOST)
            .build()
    }
}