package com.safeNest.demo.features.urlGuard1.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.urlGuard1.api.presentation.router.UrlGuardRouterConst
import com.safeNest.demo.features.urlGuard1.impl.presentation.UrlGuardActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class UrlGuardRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, UrlGuardRouterConst.HOST, UrlGuardActivity::class)