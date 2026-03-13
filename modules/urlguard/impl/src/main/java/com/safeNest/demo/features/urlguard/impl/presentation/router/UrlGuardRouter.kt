package com.safeNest.demo.features.urlGuard.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.urlGuard.api.presentation.router.UrlGuardRouterConst
import com.safeNest.demo.features.urlGuard.impl.presentation.UrlGuardActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class UrlGuardRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, UrlGuardRouterConst.HOST, UrlGuardActivity::class)