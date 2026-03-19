package com.safeNest.demo.features.callProtection.impl.domain.model

data class CallTracking(
    val phoneNumber: String,
    val date: String,
    val callCount: Int,
    val lastCalledAt: Long
)
