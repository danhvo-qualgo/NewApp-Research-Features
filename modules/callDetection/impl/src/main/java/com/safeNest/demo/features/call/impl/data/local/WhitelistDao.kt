package com.safeNest.demo.features.call.impl.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {

    @Query("SELECT * FROM whitelist")
    fun getAll(): Flow<List<WhitelistEntity>>

    @Query("SELECT * FROM whitelist WHERE phoneNumber = :phoneNumber")
    fun get(phoneNumber: String): Flow<WhitelistEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM whitelist WHERE phoneNumber = :number)")
    suspend fun exists(number: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WhitelistEntity)

    @Query("DELETE FROM whitelist WHERE phoneNumber = :number")
    suspend fun delete(number: String)
}