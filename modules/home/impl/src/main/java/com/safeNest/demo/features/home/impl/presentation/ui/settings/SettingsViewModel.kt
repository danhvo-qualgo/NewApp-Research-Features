package com.safeNest.demo.features.home.impl.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageAnalyzeModeUseCase
import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageCustomPromptUseCase
import com.safeNest.demo.features.urlGuard.api.useCase.ManageFormCheckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isRemoteMode: Boolean = false,
    val isLoading: Boolean = true,
    val customPrompt: String = "",
    val isFormCheckEnabled: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageAnalyzeModeUseCase: ManageAnalyzeModeUseCase,
    private val manageCustomPromptUseCase: ManageCustomPromptUseCase,
    private val managerFormCheckUseCase: ManageFormCheckUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMode()
        loadCustomPrompt()
        loadFormCheckEnabled()
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
    
    private fun loadCustomPrompt() {
        viewModelScope.launch {
            try {
                val prompt = manageCustomPromptUseCase.getCustomPrompt()
                _uiState.value = _uiState.value.copy(customPrompt = prompt)
            } catch (e: Exception) {
                // Use default
            }
        }
    }

    private fun loadFormCheckEnabled() {
        viewModelScope.launch {
            try {
                val formCheckEnabled = managerFormCheckUseCase.isEnabled()
                _uiState.value = _uiState.value.copy(isFormCheckEnabled = formCheckEnabled)
            } catch (e: Exception) {
                // Use default
            }
        }
    }

    fun toggleFormCheck(isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                managerFormCheckUseCase.setEnabled(isEnabled)
                _uiState.value = _uiState.value.copy(isFormCheckEnabled = isEnabled)
            } catch (e: Exception) {
                // Revert on error
                loadFormCheckEnabled()
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
    
    fun updateCustomPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(customPrompt = prompt)
    }
    
    fun saveCustomPrompt() {
        viewModelScope.launch {
            try {
                manageCustomPromptUseCase.setCustomPrompt(_uiState.value.customPrompt)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun resetToDefaultPrompt() {
        viewModelScope.launch {
            val defaultPrompt = manageCustomPromptUseCase.getDefaultPrompt()
            _uiState.value = _uiState.value.copy(customPrompt = defaultPrompt)
        }
    }
}
