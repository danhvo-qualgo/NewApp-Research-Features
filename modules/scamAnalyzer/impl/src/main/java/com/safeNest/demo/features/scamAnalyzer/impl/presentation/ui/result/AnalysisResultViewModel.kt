package com.safeNest.demo.features.scamAnalyzer.impl.presentation.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.useCase.GetAnalysisResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalysisResultUiState(
    val isLoading: Boolean = true,
    val analysisResult: AnalysisResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AnalysisResultViewModel @Inject constructor(
    private val getAnalysisResultUseCase: GetAnalysisResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisResultUiState())
    val uiState: StateFlow<AnalysisResultUiState> = _uiState.asStateFlow()

    fun loadAnalysisResult(resultKey: String?) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val result = resultKey?.let { key ->
                    getAnalysisResultUseCase(key)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    analysisResult = result,
                    errorMessage = if (result == null && resultKey != null) {
                        "Analysis result not found or expired"
                    } else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load analysis result"
                )
            }
        }
    }
}