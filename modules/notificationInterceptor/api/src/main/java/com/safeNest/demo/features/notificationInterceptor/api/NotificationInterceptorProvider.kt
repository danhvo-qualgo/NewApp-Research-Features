package com.safeNest.demo.features.notificationInterceptor.api

import android.app.Service

interface NotificationInterceptorProvider {
    fun getNotificationInterceptorClass(): Class<out Service>
}