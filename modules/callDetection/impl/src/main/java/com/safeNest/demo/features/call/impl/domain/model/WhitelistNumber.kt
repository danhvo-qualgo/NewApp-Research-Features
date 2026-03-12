package com.safeNest.demo.features.call.impl.domain.model

data class WhitelistNumber(
    val phoneNumber: String,
    val name: String = "",
    val label: String = ""
)