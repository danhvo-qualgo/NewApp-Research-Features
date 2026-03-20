package com.safeNest.demo.features.home.impl.presentation.ui.tool

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScamAnalyzerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val analysisResult: String? = null
)

sealed interface ScamAnalyzerEvent {
    data object AnalysisSuccess : ScamAnalyzerEvent
}

@HiltViewModel
class ScamAnalyzerViewModel @Inject constructor(
    private val analyzeUseCase: AnalyzeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScamAnalyzerUiState())
    val uiState: StateFlow<ScamAnalyzerUiState> = _uiState.asStateFlow()

    private val _events = Channel<ScamAnalyzerEvent>()
    val events = _events.receiveAsFlow()

    companion object {
        // Regex pattern to match URLs
        private val URL_PATTERN = Regex(
            pattern = "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$",
            option = RegexOption.IGNORE_CASE
        )
        
        // More comprehensive URL pattern
        private val URL_PATTERN_COMPREHENSIVE = Regex(
            pattern = "(https?://|www\\.)[^\\s]+",
            option = RegexOption.IGNORE_CASE
        )
    }
    
    private fun isUrl(text: String): Boolean {
        val trimmedText = text.trim()
        return URL_PATTERN_COMPREHENSIVE.containsMatchIn(trimmedText) || 
               trimmedText.startsWith("http://") || 
               trimmedText.startsWith("https://") ||
               trimmedText.startsWith("www.")
    }
    
    private fun normalizeUrl(text: String): String {
        val trimmed = text.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("www.") -> "https://$trimmed"
            else -> trimmed
        }
    }

    fun analyzeText(text: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val input = if (isUrl(text)) {
                    val url = normalizeUrl(text)
                    Log.d("AnalyzeResult", "Detected URL: $url")
                    AnalysisInput.Url(url)
                } else {
                    Log.d("AnalyzeResult", "Detected normal text")
                    AnalysisInput.Text("", "", text)
                }
                
                val result = analyzeUseCase(input)
                Log.d("AnalyzeResult", result.toString())
                
                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        analysisResult = result.toString()
                    )
                    _events.send(ScamAnalyzerEvent.AnalysisSuccess)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Something went wrong. Please try again."
                    )
                }
            } catch (e: Exception) {
                Log.e("AnalyzeResult", "Error analyzing text", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}