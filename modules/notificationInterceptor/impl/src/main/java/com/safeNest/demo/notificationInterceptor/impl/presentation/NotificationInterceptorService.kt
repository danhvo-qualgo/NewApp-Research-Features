package com.safeNest.demo.notificationInterceptor.impl.presentation

import android.app.Notification
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.safeNest.demo.notificationInterceptor.impl.data.NotificationCategory
import com.safeNest.demo.notificationInterceptor.impl.data.NotificationRecord
import com.safeNest.demo.notificationInterceptor.impl.data.NotificationStore

class NotificationInterceptorService : NotificationListenerService() {

    private var hasRebound = false

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotifInterceptor", "Listener connected — service is active")
        // Heal stale binder after reinstall: toggle the component off/on so the
        // OS re-registers the INotificationListener binder correctly.
        if (!hasRebound) {
            hasRebound = true
            val pm = packageManager
            val component = ComponentName(this, NotificationInterceptorService::class.java)
            pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        hasRebound = false
        Log.d("NotifInterceptor", "Listener disconnected — requesting rebind")
        requestRebind(ComponentName(this, NotificationInterceptorService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("NotifInterceptor", "onNotificationPosted: ${sbn.packageName}")
        val notification = sbn.notification ?: return
        val extras = notification.extras

        val category = when {
            notification.category == Notification.CATEGORY_CALL -> NotificationCategory.CALL
            notification.category == Notification.CATEGORY_MESSAGE -> {
                val smsPackage = Telephony.Sms.getDefaultSmsPackage(applicationContext)
                if (sbn.packageName == smsPackage) NotificationCategory.SMS
                else NotificationCategory.CHAT
            }
            else -> NotificationCategory.OTHER
        }

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
        val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()

        val messages: List<String>
        var senderName: String? = null
        var conversationTitle: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val rawMessages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            val parsedMessages = if (rawMessages != null) {
                Notification.MessagingStyle.Message.getMessagesFromBundleArray(rawMessages)
            } else emptyList()
            messages = parsedMessages.mapNotNull { it.text?.toString() }
            senderName = parsedMessages.lastOrNull()?.senderPerson?.name?.toString()
                ?: extras.getCharSequence(Notification.EXTRA_MESSAGING_PERSON)?.toString()
            conversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        } else {
            messages = emptyList()
        }

        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        val isForegroundService = (notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0

        val actionLabels = notification.actions
            ?.mapNotNull { it.title?.toString() }
            ?: emptyList()

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.channelId
        } else null

        val record = NotificationRecord(
            id = "${sbn.packageName}:${sbn.id}:${sbn.tag ?: ""}",
            notificationId = sbn.id,
            notificationTag = sbn.tag,
            packageName = sbn.packageName,
            appName = resolveAppName(sbn.packageName),
            category = category,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            infoText = infoText,
            summaryText = summaryText,
            messages = messages,
            conversationTitle = conversationTitle,
            senderName = senderName,
            timeMs = sbn.postTime,
            isOngoing = isOngoing,
            isForegroundService = isForegroundService,
            priority = notification.priority,
            visibility = notification.visibility,
            notificationChannelId = channelId,
            actionLabels = actionLabels,
            wasClicked = false
        )
        Log.d("NotifInterceptor", "Record saved: ${record.appName} — ${record.title}")
        NotificationStore.add(record)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap, reason: Int) {
        if (reason == REASON_CLICK) {
            val key = "${sbn.packageName}:${sbn.id}:${sbn.tag ?: ""}"
            NotificationStore.markClicked(key)
        }
    }

    private fun resolveAppName(packageName: String): String {
        return try {
            val pm = applicationContext.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
