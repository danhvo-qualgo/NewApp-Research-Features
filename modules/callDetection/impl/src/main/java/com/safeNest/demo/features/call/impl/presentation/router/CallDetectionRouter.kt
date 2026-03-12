package com.safeNest.demo.features.call.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.call.api.router.CallDetectionRouterConst
import com.safeNest.demo.features.call.impl.presentation.CallDetectionActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CallDetectionRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, CallDetectionRouterConst.HOST, CallDetectionActivity::class)