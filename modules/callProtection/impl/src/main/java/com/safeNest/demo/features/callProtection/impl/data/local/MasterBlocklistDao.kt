package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterBlocklistDao {

    @Query("SELECT * FROM master_blocklist")
    fun getAll(): Flow<List<MasterBlocklistEntity>>

    @Query("SELECT * FROM master_blocklist WHERE normalizedNumber = :normalizedNumber")
    fun get(normalizedNumber: String): Flow<MasterBlocklistEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM master_blocklist WHERE normalizedNumber = :normalizedNumber)")
    suspend fun exists(normalizedNumber: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MasterBlocklistEntity)

    @Query("DELETE FROM master_blocklist WHERE normalizedNumber = :normalizedNumber")
    suspend fun delete(normalizedNumber: String)
}