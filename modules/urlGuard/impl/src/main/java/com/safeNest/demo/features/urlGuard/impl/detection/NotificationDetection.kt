package com.safeNest.demo.features.urlGuard.impl.detection

import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus

interface NotificationDetection {
    suspend fun detectNotificationContent(text: String): DetectionStatus
}