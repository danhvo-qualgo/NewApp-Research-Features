package com.safeNest.demo.features.scamAnalyzer.api.models

import android.content.Context
import android.net.Uri

sealed class AnalysisInput {
    data class Text(val text: String) : AnalysisInput()

    data class Url(val url: String) : AnalysisInput()

    data class Image(val uri: Uri, val context: Context) : AnalysisInput()

    data class Audio(val uri: Uri) : AnalysisInput()
}