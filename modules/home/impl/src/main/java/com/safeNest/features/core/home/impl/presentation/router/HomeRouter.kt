package com.safeNest.features.core.home.impl.presentation.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.uney.core.router.Router
import com.safeNest.features.core.home.impl.presentation.HomeActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val DEEPLINK_SCHEME = "internal"
private const val DEEPLINK_HOST = "home"

class HomeRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : Router() {
    override fun canHandle(deeplink: Uri): Boolean {
        return deeplink.scheme == DEEPLINK_SCHEME && deeplink.host == DEEPLINK_HOST
    }

    override fun getLaunchIntent(deeplink: Uri): Intent {
        return Intent(context, HomeActivity::class.java).apply {
            data = deeplink
        }
    }
}