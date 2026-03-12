package com.safeNest.demo.features.callProtection.impl.domain.model

data class WhitelistNumber(
    val phoneNumber: String,
    val name: String = "",
    val label: String = ""
)