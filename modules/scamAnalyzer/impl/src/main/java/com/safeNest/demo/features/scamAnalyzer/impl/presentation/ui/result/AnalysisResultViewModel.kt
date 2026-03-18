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

    init {
        loadAnalysisResult()
    }

    private fun loadAnalysisResult() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val result = getAnalysisResultUseCase()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    analysisResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load analysis result"
                )
            }
        }
    }

    fun retry() {
        loadAnalysisResult()
    }
}