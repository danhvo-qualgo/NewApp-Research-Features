package com.safeNest.demo.call.main.impl.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklist_pattern")
data class BlacklistPatternEntity(
    @PrimaryKey val pattern: String
)