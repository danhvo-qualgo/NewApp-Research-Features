package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.safeNest.demo.features.callProtection.impl.domain.model.BlacklistPattern

@Entity(tableName = "blacklist_pattern")
data class BlacklistPatternEntity(
    @PrimaryKey val pattern: String,
    val description: String
) {
    fun toBlacklistPattern(): BlacklistPattern {
        return BlacklistPattern(pattern, description)
    }
}