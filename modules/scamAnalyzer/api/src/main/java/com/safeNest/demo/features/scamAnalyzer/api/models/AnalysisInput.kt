package com.safeNest.demo.features.scamAnalyzer.api.models

import android.net.Uri

sealed class AnalysisInput {
    data class Text(
        val senderId: String = "",
        val bundle: String = "",
        val text: String,
    ) : AnalysisInput()

    data class Url(
        val url: String
    ) : AnalysisInput()

    data class Image(
        val uri: Uri,
    ) : AnalysisInput()

    data class Audio(
        val uri: Uri,
    ) : AnalysisInput()
}