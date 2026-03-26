package com.safeNest.demo.features.scamAnalyzer.impl.domain.models

import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisItem
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeKeyFinding(
    val category: String="",
    val description: String="",
    val evidence: String="",
    val severity: String="",
)

@Serializable
enum class AnalyzeVerdict {
    @SerialName("scam")
    Scam,

    @SerialName("safe")
    Safe,

    @SerialName("suspicious")
    Unverified;
}

fun AnalyzeVerdict.toAnalysisStatus(): AnalysisStatus {
    return when (this) {
        AnalyzeVerdict.Scam -> AnalysisStatus.Scam
        AnalyzeVerdict.Safe -> AnalysisStatus.Safe
        AnalyzeVerdict.Unverified -> AnalysisStatus.Unverified
    }
}

fun AnalyzeKeyFinding.toAnalysisItem(): AnalysisItem {
    return AnalysisItem(
        title = description,
        description = evidence,
    )
}

/*
    bundleId: application id
    sender: ""
    text:
 */