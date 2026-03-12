package com.safeNest.demo.notificationInterceptor.impl.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NotificationStore {
    private val _notifications = MutableStateFlow<List<NotificationRecord>>(emptyList())
    val notifications: StateFlow<List<NotificationRecord>> = _notifications

    fun add(record: NotificationRecord) {
        val current = _notifications.value
        val existing = current.indexOfFirst { it.id == record.id }
        _notifications.value = if (existing >= 0) {
            current.toMutableList().also { it[existing] = record }
        } else {
            current + record
        }
    }

    fun markClicked(key: String) {
        _notifications.value = _notifications.value.map { record ->
            if (record.id == key) record.copy(wasClicked = true) else record
        }
    }
}
