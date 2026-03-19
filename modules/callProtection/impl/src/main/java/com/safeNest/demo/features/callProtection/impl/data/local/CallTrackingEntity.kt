package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.safeNest.demo.features.callProtection.impl.domain.model.CallTracking

@Entity(
    tableName = "call_tracking",
    primaryKeys = ["phoneNumber", "date"]
)
data class CallTrackingEntity(
    val phoneNumber: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "callCount")
    val callCount: Int = 1,

    @ColumnInfo(name = "lastCalledAt")
    val lastCalledAt: Long = System.currentTimeMillis()
) {
    fun toCallTracking() = CallTracking(
        phoneNumber = phoneNumber,
        date = date,
        callCount = callCount,
        lastCalledAt = lastCalledAt
    )
}