package com.safeNest.demo.features.notificationInterceptor.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.notificationInterceptor.api.model.NotificationCategory
import com.safeNest.demo.features.notificationInterceptor.impl.data.NotificationRecord
import com.safeNest.demo.features.notificationInterceptor.impl.data.NotificationStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NotificationUiState(
    val chatNotifications: List<NotificationRecord> = emptyList(),
    val smsNotifications: List<NotificationRecord> = emptyList(),
    val callNotifications: List<NotificationRecord> = emptyList(),
    val otherNotifications: List<NotificationRecord> = emptyList()
)

@HiltViewModel
class NotificationInterceptorViewModel @Inject constructor() : ViewModel() {

    val uiState: StateFlow<NotificationUiState> = NotificationStore.notifications
        .map { notifications ->
            NotificationUiState(
                chatNotifications = notifications.filter { it.category == NotificationCategory.CHAT }
                    .sortedByDescending { it.timeMs },
                smsNotifications = notifications.filter { it.category == NotificationCategory.SMS }
                    .sortedByDescending { it.timeMs },
                callNotifications = notifications.filter { it.category == NotificationCategory.CALL }
                    .sortedByDescending { it.timeMs },
                otherNotifications = notifications.filter { it.category == NotificationCategory.OTHER }
                    .sortedByDescending { it.timeMs }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationUiState()
        )
}
