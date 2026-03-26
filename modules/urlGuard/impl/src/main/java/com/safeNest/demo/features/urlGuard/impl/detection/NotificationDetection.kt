package com.safeNest.demo.features.urlGuard.impl.detection

import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.DetectionStatus

interface NotificationDetection {
    suspend fun detectNotificationContent(text: String): DetectionStatus
}