package com.safeNest.demo.features.notificationInterceptor.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.notificationInterceptor.api.presentation.router.NotificationInterceptorRouterConst
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import net.qualgo.safeNest.features.notificationInterceptor.impl.presentation.NotificationInterceptorActivity

class NotificationInterceptorRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(
    context,
    NotificationInterceptorRouterConst.HOST,
    NotificationInterceptorActivity::class
)