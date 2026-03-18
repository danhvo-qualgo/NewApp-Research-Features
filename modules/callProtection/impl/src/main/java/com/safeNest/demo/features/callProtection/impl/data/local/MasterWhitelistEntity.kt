package com.safeNest.demo.features.callProtection.impl.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.safeNest.demo.features.callProtection.impl.domain.model.PhoneNumberInfo

@Entity(tableName = "master_whitelist")
data class MasterWhitelistEntity(
    val phoneNumber: String,
    val name: String = "",
    val label: String = "",
    @PrimaryKey
    val normalizedNumber: String = ""
) {
    fun toPhoneNumberInfo(): PhoneNumberInfo {
        return PhoneNumberInfo(phoneNumber, name, label, normalizedNumber)
    }
}

