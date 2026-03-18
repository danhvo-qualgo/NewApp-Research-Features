package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterWhitelistDao {

    @Query("SELECT * FROM master_whitelist")
    fun getAll(): Flow<List<MasterWhitelistEntity>>

    @Query("SELECT * FROM master_whitelist WHERE normalizedNumber = :normalizedNumber")
    fun get(normalizedNumber: String): Flow<MasterWhitelistEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM master_whitelist WHERE normalizedNumber = :normalizedNumber)")
    suspend fun exists(normalizedNumber: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MasterWhitelistEntity)

    @Query("DELETE FROM master_whitelist WHERE normalizedNumber = :normalizedNumber")
    suspend fun delete(normalizedNumber: String)
}