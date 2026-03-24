package com.safeNest.demo.features.commonKotlin

import kotlinx.coroutines.flow.MutableSharedFlow

val incomingCallSharedFlow: MutableSharedFlow<IncomingCallData> = MutableSharedFlow(1)

data class IncomingCallData(
    val phoneNumber: String,
    val message: String
)

enum class IncomingCallType {
    BLOCKLIST, WHITELIST, CALLER_ID
}

