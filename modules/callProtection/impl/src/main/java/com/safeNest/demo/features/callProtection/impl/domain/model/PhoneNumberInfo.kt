package com.safeNest.demo.features.callProtection.impl.domain.model

import com.safeNest.demo.features.callProtection.impl.data.local.MasterBlocklistEntity
import com.safeNest.demo.features.callProtection.impl.data.local.MasterWhitelistEntity
import com.safeNest.demo.features.callProtection.impl.data.local.WhitelistEntity

data class PhoneNumberInfo(
    val phoneNumber: String,
    val name: String = "",
    val label: String = "",
    val normalizedNumber: String = "",
    val type: PhoneNumberInfoType = PhoneNumberInfoType.SAFE
) {
    fun toWhitelistEntity(): WhitelistEntity {
        return WhitelistEntity(phoneNumber, name, label, normalizedNumber)
    }

    fun toMasterWhitelistEntity(): MasterWhitelistEntity {
        return MasterWhitelistEntity(phoneNumber, name, label, normalizedNumber)
    }

    fun toMasterBlocklistEntity(): MasterBlocklistEntity {
        return MasterBlocklistEntity(phoneNumber, name, label, normalizedNumber)
    }
}

enum class PhoneNumberInfoType {
    SCAM, SPAM, PHISHING, SAFE, UNKNOW
}