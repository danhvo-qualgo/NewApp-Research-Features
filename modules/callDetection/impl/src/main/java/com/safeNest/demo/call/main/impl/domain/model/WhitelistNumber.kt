package com.safeNest.demo.call.main.impl.domain.model

data class WhitelistNumber(
    val phoneNumber: String,
    val name: String = "",
    val label: String = ""
)