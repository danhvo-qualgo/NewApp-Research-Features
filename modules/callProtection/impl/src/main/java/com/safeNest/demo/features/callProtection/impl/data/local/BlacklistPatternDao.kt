package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistPatternDao {

    @Query("SELECT * FROM blacklist_pattern")
    fun getAll(): Flow<List<BlacklistPatternEntity>>

    @Query("SELECT * FROM blacklist_pattern WHERE pattern = :pattern")
    fun get(pattern: String): Flow<BlacklistPatternEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BlacklistPatternEntity)

    @Query("DELETE FROM blacklist_pattern WHERE pattern = :pattern")
    suspend fun delete(pattern: String)
}