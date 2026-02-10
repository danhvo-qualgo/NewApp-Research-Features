package net.qualgo.safeNest.core.authChallenge.impl.data.source

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class AuthChallengeRequest(
    val authFlow: String,
    val challengeName: String,
    val clientMetadata: JsonObject? = null,
    val session: String? = null, // previous session
    val challengeResponses: JsonObject? = null // the whole previous response
)