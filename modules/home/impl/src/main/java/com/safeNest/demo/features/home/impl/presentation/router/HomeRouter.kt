package com.safeNest.demo.features.home.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.home.api.presentation.router.HomeRouterConst
import com.safeNest.demo.features.home.impl.presentation.HomeActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HomeRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, HomeRouterConst.HOST, HomeActivity::class)