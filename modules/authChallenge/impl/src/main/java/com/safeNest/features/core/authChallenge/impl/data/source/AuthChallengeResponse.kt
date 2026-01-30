package com.safeNest.features.core.authChallenge.impl.data.source

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class AuthChallengeResponse(
    val authFlow: String,
    val challengeName: String,
    val session: String? = null,
    val challengeParameters: JsonObject? = null
)