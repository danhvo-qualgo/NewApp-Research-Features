package com.safeNest.features.call.callDetection.impl.presentation.router

import android.content.Context
import com.safeNest.features.call.callDetection.api.router.CallDetectionRouterConst
import com.safeNest.features.call.callDetection.impl.presentation.CallDetectionActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CallDetectionRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, CallDetectionRouterConst.HOST, CallDetectionActivity::class)