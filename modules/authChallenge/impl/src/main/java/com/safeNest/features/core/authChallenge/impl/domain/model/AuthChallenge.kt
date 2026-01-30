package com.safeNest.features.core.authChallenge.impl.domain.model

import kotlinx.serialization.json.JsonObject

data class AuthChallenge(
    val authFlow: String,
    val challengeName: String,
    val clientMetadata: JsonObject? = null,
    val session: String? = null, // previous session
    val challengeParameters: JsonObject? = null,
    val challengeResponses: JsonObject? = null // the whole previous response
) {
    companion object {
        val EMPTY = AuthChallenge(authFlow = "", challengeName = "")
    }

    fun toString(
        clientMetadata: Boolean = false,
        challengeParameters: Boolean = false,
        challengeResponses: Boolean = false
    ): String {
        val builder = StringBuilder()
        builder.append("• authFlow: $authFlow")
        builder.startWithLine("• challengeName: $challengeName")
        builder.startWithLine("• session: $session")
        if (clientMetadata) {
            builder.startWithLine("• clientMetadata: ${this.clientMetadata}\n")
        }
        if (challengeParameters) {
            builder.startWithLine("• challengeParameters: ${this.challengeParameters}\n")
        }
        if (challengeResponses) {
            builder.startWithLine("• challengeResponses: ${this.challengeResponses}\n")
        }
        return builder.toString()
    }

    private fun StringBuilder.startWithLine(text: String): StringBuilder =
        this.appendLine().append(text)
}