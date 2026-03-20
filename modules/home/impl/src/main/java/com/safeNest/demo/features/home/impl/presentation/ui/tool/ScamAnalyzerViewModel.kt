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

    fun analyzeText(text: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val result = analyzeUseCase(AnalysisInput.Text("", "", text))
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

    fun analyzeAudio(audioUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val result = analyzeUseCase(AnalysisInput.Audio(audioUri))
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
                Log.e("AnalyzeResult", "Error analyzing audio", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun analyzeImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val result = analyzeUseCase(AnalysisInput.Image(imageUri))
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
                Log.e("AnalyzeResult", "Error analyzing image", e)
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