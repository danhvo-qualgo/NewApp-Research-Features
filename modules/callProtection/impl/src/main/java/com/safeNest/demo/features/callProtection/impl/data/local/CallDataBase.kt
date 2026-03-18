package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WhitelistEntity::class, BlacklistPatternEntity::class, MasterWhitelistEntity::class, MasterBlocklistEntity::class], version = 2)
abstract class CallDataBase : RoomDatabase() {
    abstract fun whitelistDao(): WhitelistDao
    abstract fun blacklistPatternDao(): BlacklistPatternDao
    abstract fun masterWhitelistDao(): MasterWhitelistDao
    abstract fun masterBlocklistDao(): MasterBlocklistDao

}