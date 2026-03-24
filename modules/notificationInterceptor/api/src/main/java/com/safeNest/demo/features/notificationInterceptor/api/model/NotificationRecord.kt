package com.safeNest.demo.features.notificationInterceptor.api.model

data class NotificationRecord(
    val sender: String?,
    val appSenderPkgName: String,
    val category: NotificationCategory,
    val title: String?,
    val content: String?,
)
