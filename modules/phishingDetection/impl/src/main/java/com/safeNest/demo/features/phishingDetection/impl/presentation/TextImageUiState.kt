package net.qualgo.safeNest.features.phishingDetection.impl.presentation

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
    data class Error(val message: String) : TextImageUiState()
}
