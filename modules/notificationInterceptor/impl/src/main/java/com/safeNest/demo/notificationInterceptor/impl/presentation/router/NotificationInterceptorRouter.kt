package com.safeNest.demo.notificationInterceptor.impl.presentation.router

import android.content.Context
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import com.safeNest.demo.notificationInterceptor.api.presentation.router.NotificationInterceptorRouterConst
import com.safeNest.demo.notificationInterceptor.impl.presentation.NotificationInterceptorActivity

class NotificationInterceptorRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
): InternalRouter(context, NotificationInterceptorRouterConst.HOST, NotificationInterceptorActivity::class)