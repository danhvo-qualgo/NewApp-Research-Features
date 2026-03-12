package com.safeNest.demo.notificationInterceptor.impl.data

data class NotificationRecord(
    // Identity
    val id: String,
    val notificationId: Int,
    val notificationTag: String?,
    val packageName: String,
    val appName: String,
    val category: NotificationCategory,

    // Content
    val title: String?,
    val text: String?,
    val subText: String?,
    val bigText: String?,
    val infoText: String?,
    val summaryText: String?,
    val messages: List<String>,       // MessagingStyle messages (WhatsApp, SMS, etc.)
    val conversationTitle: String?,   // MessagingStyle group/conversation name

    // Sender (MessagingStyle)
    val senderName: String?,

    // Timing
    val timeMs: Long,

    // Behaviour flags
    val isOngoing: Boolean,
    val isForegroundService: Boolean,
    val priority: Int,                // Notification.PRIORITY_*
    val visibility: Int,              // Notification.VISIBILITY_*
    val notificationChannelId: String?,

    // Actions
    val actionLabels: List<String>,

    // Status
    val wasClicked: Boolean = false
)
