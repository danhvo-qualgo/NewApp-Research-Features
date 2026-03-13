package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.safeNest.demo.features.callProtection.impl.domain.model.WhitelistNumber

@Entity(tableName = "whitelist")
data class WhitelistEntity(
    @PrimaryKey val phoneNumber: String,
    val name: String = "",
    val label: String = ""
) {
    fun toWhitelistNumber(): WhitelistNumber {
        return WhitelistNumber(phoneNumber, name, label)
    }
}

