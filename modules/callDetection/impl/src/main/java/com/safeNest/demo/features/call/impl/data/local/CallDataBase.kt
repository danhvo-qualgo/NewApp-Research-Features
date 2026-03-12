package com.safeNest.demo.features.call.impl.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WhitelistEntity::class, BlacklistPatternEntity::class], version = 2)
abstract class CallDataBase : RoomDatabase() {
    abstract fun whitelistDao(): WhitelistDao
    abstract fun blacklistPatternDao(): BlacklistPatternDao
}