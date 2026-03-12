package com.safeNest.demo.urlguard.impl.presentation.router

import android.content.Context
import com.safeNest.demo.urlguard.api.presentation.router.UrlGuardRouterConst
import com.safeNest.demo.urlguard.impl.presentation.UrlGuardActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class UrlGuardRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, UrlGuardRouterConst.HOST, UrlGuardActivity::class)