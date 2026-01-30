package com.safeNest.features.core.authChallenge.impl.domain.useCase

import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal fun toJsonObject(from: AuthChallenge): JsonObject {
    return buildJsonObject {
        put("authFlow", from.authFlow)
        put("challengeName", from.challengeName)
        from.clientMetadata?.let { put("clientMetadata", it) }
        from.session?.let { put("session", it) }
        from.challengeParameters?.let { put("challengeParameters", it) }
        from.challengeResponses?.let { put("challengeResponses", it) }
    }
}

internal fun toAuthChallenge(from: JsonObject): AuthChallenge {
    return AuthChallenge(
        authFlow = from["authFlow"]?.jsonPrimitive?.content ?: "",
        challengeName = from["challengeName"]?.jsonPrimitive?.content ?: "",
        clientMetadata = from["clientMetadata"]?.jsonObject,
        session = from["session"]?.jsonPrimitive?.content,
        challengeParameters = from["challengeParameters"]?.jsonObject,
        challengeResponses = from["challengeResponses"]?.jsonObject
    )
}