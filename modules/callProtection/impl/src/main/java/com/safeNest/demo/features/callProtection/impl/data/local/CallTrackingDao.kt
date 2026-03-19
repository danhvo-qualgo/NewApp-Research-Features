package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CallTrackingDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewRecord(record: CallTrackingEntity): Long

    @Query("""
        UPDATE call_tracking 
        SET callCount = callCount + 1, lastCalledAt = :timestamp 
        WHERE phoneNumber = :phone AND date = :date
    """)
    suspend fun incrementCallCount(phone: String, date: String, timestamp: Long)

    @Transaction
    suspend fun trackIncomingCall(phone: String, date: String) {
        val currentTime = System.currentTimeMillis()
        val record = CallTrackingEntity(phone, date, 1, currentTime)

        val id = insertNewRecord(record)

        if (id == -1L) {
            incrementCallCount(phone, date, currentTime)
        }
    }

    @Query("SELECT * FROM call_tracking WHERE phoneNumber = :phone AND date = :date")
    suspend fun getCallCountToday(phone: String, date: String): CallTrackingEntity?

    @Query("SELECT * FROM call_tracking WHERE date = :date ORDER BY callCount DESC LIMIT :limit")
    suspend fun getTopSpammersToday(date: String, limit: Int = 10): List<CallTrackingEntity>
}