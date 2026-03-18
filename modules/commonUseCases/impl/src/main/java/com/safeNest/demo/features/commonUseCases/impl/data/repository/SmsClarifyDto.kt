package com.safeNest.demo.features.commonUseCases.impl.data.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SmsClarifyRequest(
    val entityMapping: JsonObject,
    val redactedMessage: String
)

@Serializable
data class SmsClarifyResponse(
    val riskScore: Float?,
    val confidence: Float?,
    val verdict: String?,
    val keyFindings: List<KeyFindings>?
) {
    @Serializable
    data class KeyFindings(
        val category: String?,
        val description: String?,
        val severity: String?,
        val evidence: String
    )
}