package com.safeNest.demo.features.splash.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.splash.api.router.SplashRouterConst
import com.safeNest.demo.features.splash.impl.presentation.SplashActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SplashRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, SplashRouterConst.HOST, SplashActivity::class)