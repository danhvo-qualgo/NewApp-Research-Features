package com.safeNest.demo.features.scamAnalyzer.api.models

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AnalysisStatus {
    @SerialName("Safe")
    Safe,
    @SerialName("Scam")
    Scam,
    @SerialName("Unverified")
    Unverified,
}

sealed interface AnalysisResultType {
    data class Text(val rawText: String, val redactedMessage: String) : AnalysisResultType
    data class Url(val url: String) : AnalysisResultType
    data class Image(val uri: String, val hasText: Boolean) : AnalysisResultType
    data class Audio(val uri: String) : AnalysisResultType
}

data class AnalysisItem(
    val title: String,
    val description: String
)

data class AnalysisResult(
    val data: AnalysisResultType,
    val status: AnalysisStatus,
    val keyFindings: List<AnalysisItem>,
)