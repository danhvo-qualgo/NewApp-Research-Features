package com.safeNest.demo.call.main.impl.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistEntity(
    @PrimaryKey val phoneNumber: String,
    val name: String = "",
    val label: String = ""
)