package com.safeNest.demo.features.splash.impl.data.handler

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.safeNest.demo.features.commonAndroid.SpecialPermission
import com.safeNest.demo.features.commonAndroid.isNotificationListenerEnabled
import com.safeNest.demo.features.commonAndroid.openPermissionSettings
import com.safeNest.demo.features.notificationInterceptor.api.NotificationInterceptorProvider
import com.safeNest.demo.features.splash.impl.domain.handler.PermissionHandler
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import javax.inject.Inject


internal class NotificationListenerPermissionHandler @Inject constructor(
    private val notificationInterceptorProvider: NotificationInterceptorProvider
) : PermissionHandler {

    override val type: PermissionType = PermissionType.NOTIFICATION_LISTENER

    override fun isGranted(context: Context): Boolean = context.isNotificationListenerEnabled(notificationInterceptorProvider.getNotificationInterceptorClass())

    override fun requestPermission(context: Context) {
        context.openPermissionSettings(SpecialPermission.NOTIFICATION_LISTENER)
    }
}
