package com.safeNest.demo.features.notificationInterceptor.impl.presentation

import android.util.Log
import com.safeNest.demo.features.notificationInterceptor.api.NotificationObserver
import com.safeNest.demo.features.notificationInterceptor.api.model.NotificationRecord
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class NotificationEventListener @Inject constructor() : NotificationObserver {

    private val _notificationFlow = MutableSharedFlow<NotificationRecord>(extraBufferCapacity = 64)
    override val notificationFlow: Flow<NotificationRecord> = _notificationFlow.asSharedFlow()

    internal fun tryEmit(record: NotificationRecord) {
        Log.d(TAG, "tryEmit: $record")
        _notificationFlow.tryEmit(record)
    }

    internal suspend fun emit(record: NotificationRecord) {
        Log.d(TAG, "emit: $record")
        _notificationFlow.emit(record)
    }

    companion object {
        const val TAG = "NotificationEventListener"
    }
}
