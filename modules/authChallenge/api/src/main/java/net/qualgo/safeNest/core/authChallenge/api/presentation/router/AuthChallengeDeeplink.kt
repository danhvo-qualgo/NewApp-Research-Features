package net.qualgo.safeNest.core.authChallenge.api.presentation.router

import android.net.Uri
import com.uney.core.router.InternalRouter
import kotlinx.serialization.json.JsonObject

object AuthChallengeDeeplink {

    fun entryPoint(data: JsonObject, showSsoScreenUi: Boolean): Uri {
        return Uri.Builder()
            .scheme(InternalRouter.INTERNAL_SCHEME)
            .authority(AuthChallengeRouterConst.HOST)
            .appendQueryParameter(
                AuthChallengeRouterConst.PARAM_AUTH_CHALLENGE,
                data.toString()
            ).appendQueryParameter(
                AuthChallengeRouterConst.PARAM_MOCK_SHOW_SSO_SCREEN_UI,
                showSsoScreenUi.toString()
            )
            .build()
    }
}