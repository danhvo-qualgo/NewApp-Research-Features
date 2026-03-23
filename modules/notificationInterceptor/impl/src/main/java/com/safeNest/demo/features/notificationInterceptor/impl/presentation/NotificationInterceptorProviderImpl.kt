package com.safeNest.demo.features.notificationInterceptor.impl.presentation

import android.app.Service
import com.safeNest.demo.features.notificationInterceptor.api.NotificationInterceptorProvider
import javax.inject.Inject

class NotificationInterceptorProviderImpl @Inject constructor() : NotificationInterceptorProvider {
    override fun getNotificationInterceptorClass(): Class<out Service> = NotificationInterceptorService::class.java
}