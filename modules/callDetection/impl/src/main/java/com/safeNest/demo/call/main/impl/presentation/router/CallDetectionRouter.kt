package com.safeNest.demo.call.main.impl.presentation.router

import android.content.Context
import com.safeNest.demo.call.main.api.router.CallDetectionRouterConst
import com.safeNest.demo.call.main.impl.presentation.CallDetectionActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CallDetectionRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, CallDetectionRouterConst.HOST, CallDetectionActivity::class)