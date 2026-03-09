package com.safeNest.features.call.callDetection.impl.domain.model

data class WhitelistNumber(
    val phoneNumber: String,
    val name: String = "",
    val label: String = ""
)