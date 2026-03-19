package com.safeNest.demo.features.home.impl.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageAnalyzeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isRemoteMode: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageAnalyzeModeUseCase: ManageAnalyzeModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMode()
    }

    private fun loadCurrentMode() {
        viewModelScope.launch {
            try {
                val currentMode = manageAnalyzeModeUseCase.getMode()
                _uiState.value = _uiState.value.copy(
                    isRemoteMode = currentMode == AnalyzeMode.Remote,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleAnalyzeMode(isRemote: Boolean) {
        viewModelScope.launch {
            try {
                val newMode = if (isRemote) AnalyzeMode.Remote else AnalyzeMode.Local
                manageAnalyzeModeUseCase.setMode(newMode)
                _uiState.value = _uiState.value.copy(isRemoteMode = isRemote)
            } catch (e: Exception) {
                // Revert on error
                loadCurrentMode()
            }
        }
    }
}
