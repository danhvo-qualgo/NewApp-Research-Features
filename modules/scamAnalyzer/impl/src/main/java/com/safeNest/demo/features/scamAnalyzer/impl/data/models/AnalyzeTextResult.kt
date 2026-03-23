package com.safeNest.demo.features.scamAnalyzer.impl.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeTextResult(
    val keyFindings: List<AnalyzeKeyFinding>,
    val redactedMessage: String,
    val riskScore: Float,
    val verdict: AnalyzeVerdict
)

@Serializable
data class AnalyzeAudioResult(
    val confidence: Float,
    val keyFindings: List<AnalyzeKeyFinding>,
    val riskScore: Float,
    val verdict: AnalyzeVerdict
)

@Serializable
data class AnalyzeImageResult(
    val hasText: Boolean,
    val keyFindings: List<AnalyzeKeyFinding>,
    val riskScore: Float,
    val verdict: AnalyzeVerdict,
)

@Serializable
data class AnalyzeUrlResult(
    val riskScore: Float,
    val keyFindings: List<AnalyzeKeyFinding>,
    val verdict: AnalyzeVerdict
)