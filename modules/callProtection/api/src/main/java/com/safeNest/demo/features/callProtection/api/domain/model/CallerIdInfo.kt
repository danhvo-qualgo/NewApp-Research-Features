package com.safeNest.demo.features.callProtection.api.domain.model

data class CallerIdInfo(
    val phoneNumber: String,
    val label: String,
    val type: CallerIdInfoType
)

enum class CallerIdInfoType {
    SCAM, SPAM, PHISHING, SAFE, UNKNOW
}