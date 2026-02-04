package com.safeNest.features.core.home.impl.presentation.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.uney.core.router.Router
import com.safeNest.features.core.home.impl.presentation.HomeActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val DEEPLINK_HOST = "home"

class HomeRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, DEEPLINK_HOST, HomeActivity::class)