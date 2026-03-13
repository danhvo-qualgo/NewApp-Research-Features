package com.safeNest.features.call.callDetection.impl.domain.model

import com.safeNest.features.call.callDetection.impl.data.local.WhitelistEntity

data class WhitelistNumber(
    val phoneNumber: String,
    val name: String = "",
    val label: String = ""
) {
    fun toWhitelistEntity(): WhitelistEntity {
        return WhitelistEntity(phoneNumber, name, label)
    }
}