package com.safeNest.demo.features.notificationInterceptor.api

import com.safeNest.demo.features.notificationInterceptor.api.model.NotificationRecord
import kotlinx.coroutines.flow.Flow

interface NotificationObserver {
    val notificationFlow: Flow<NotificationRecord>
}
