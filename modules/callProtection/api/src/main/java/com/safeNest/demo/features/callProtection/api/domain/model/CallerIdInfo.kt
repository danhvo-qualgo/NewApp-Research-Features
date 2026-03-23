package com.safeNest.demo.features.callProtection.api.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CallerIdInfo(
    val phoneNumber: String,
    val label: String,
    val type: CallerIdInfoType
)

@Serializable
enum class CallerIdInfoType {
    SCAM, SPAM, PHISHING, SAFE, UNKNOW
}