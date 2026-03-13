package com.safeNest.demo.features.callProtection.impl.domain.model

import com.safeNest.demo.features.callProtection.impl.data.local.WhitelistEntity

data class WhitelistNumber(
    val phoneNumber: String,
    val name: String = "",
    val label: String = ""
) {
    fun toWhitelistEntity(): WhitelistEntity {
        return WhitelistEntity(phoneNumber, name, label)
    }
}