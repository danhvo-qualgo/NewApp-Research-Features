package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities

enum class ExtractionMethod { REGEX, LLM }

sealed class TextImageUiState {
    object Idle : TextImageUiState()
    object OcrRunning : TextImageUiState()
    data class Extracting(
        val method: ExtractionMethod,
        val sourceText: String,
        val partialOutput: String = "",
    ) : TextImageUiState()
    data class Done(
        val sourceText: String,
        val entities: ExtractedEntities,
    ) : TextImageUiState()
    data class DeepAnalyzing(
        val sourceText: String,
        val entities: ExtractedEntities,
        val partialOutput: String = "",
    ) : TextImageUiState()
    data class AnalysisComplete(
        val sourceText: String,
        val entities: ExtractedEntities,
        val redactedText: String,
        val summary: String,
        val analysis: String,
    ) : TextImageUiState()
    data class Error(val message: String) : TextImageUiState()
}
