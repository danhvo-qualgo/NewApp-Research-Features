package com.safeNest.demo.features.scamAnalyzer.api.models

import android.net.Uri

enum class AnalysisResultStatus {
    Safe,
    Scam,
    Unverified,
}

data class AnalysisItem(
    val title: String,
    val description: String
)

sealed class AnalysisResult(
    open val status: AnalysisResultStatus,
    open val analysisItems: List<AnalysisItem>?
) {

    data class Text(
        val originalText: String,
        val maskedText: String,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)

    data class Url(
        val url: String,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)

    data class Image(
        val imageUri: Uri,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)

    data class Audio(
        val audioUri: Uri,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)
}
