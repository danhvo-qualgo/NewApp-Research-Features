package com.safeNest.demo.features.home.impl.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageAnalyzeModeUseCase
import com.safeNest.demo.features.urlGuard.api.useCase.ManageFormCheckUseCase
import com.safenest.urlanalyzer.ModelManager
import com.safenest.urlanalyzer.ModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isRemoteMode: Boolean = false,
    val isLoading: Boolean = true,
    val isFormCheckEnabled: Boolean = false,
    val isDownloadingModel: Boolean = false,
    val isDeletingModel: Boolean = false,
    /** True when the user started [downloadModel] and the GGUF was already on disk (show completed + loading engine, not percent). */
    val modelAlreadyOnDisk: Boolean = false,
    /** True when the GGUF exists on disk ([ModelManager.isModelDownloaded]). */
    val isModelDownloaded: Boolean = false,
    /** 0–100 from [ModelState.Loading] while the model is downloading or loading into memory. */
    val modelDownloadProgress: Int = 0,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val manageAnalyzeModeUseCase: ManageAnalyzeModeUseCase,
    private val managerFormCheckUseCase: ManageFormCheckUseCase,
    private val modelManager: ModelManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMode()
        loadFormCheckEnabled()
        syncModelDownloadStateFromManager()
        viewModelScope.launch {
            modelManager.state.collect { state ->
                _uiState.update { ui ->
                    when (state) {
                        is ModelState.Loading -> ui.copy(
                            isDownloadingModel = true,
                            modelDownloadProgress = state.progress,
                            isModelDownloaded = modelManager.isModelDownloaded(),
                        )

                        is ModelState.Ready -> ui.copy(
                            isDownloadingModel = false,
                            modelDownloadProgress = 0,
                            modelAlreadyOnDisk = false,
                            isModelDownloaded = true,
                        )

                        is ModelState.Error -> ui.copy(
                            isDownloadingModel = false,
                            modelDownloadProgress = 0,
                            modelAlreadyOnDisk = false,
                            isModelDownloaded = modelManager.isModelDownloaded(),
                        )

                        ModelState.Uninitialized -> ui.copy(
                            isDownloadingModel = false,
                            modelDownloadProgress = 0,
                            modelAlreadyOnDisk = false,
                            isModelDownloaded = modelManager.isModelDownloaded(),
                        )
                    }
                }
            }
        }
    }

    private fun syncModelDownloadStateFromManager() {
        _uiState.update { it.copy(isModelDownloaded = modelManager.isModelDownloaded()) }
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

    fun downloadModel() {
        if (_uiState.value.isDownloadingModel || _uiState.value.isDeletingModel) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(modelAlreadyOnDisk = modelManager.isModelDownloaded())
            }
            try {
                modelManager.initialize()
            } catch (_: Exception) {
            } finally {
                _uiState.update { it.copy(modelAlreadyOnDisk = false) }
            }
        }
    }

    fun deleteModel() {
        if (_uiState.value.isDownloadingModel || _uiState.value.isDeletingModel) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingModel = true) }
            try {
                modelManager.delete()
            } catch (_: Exception) {
            } finally {
                _uiState.update {
                    it.copy(
                        isDeletingModel = false,
                        isModelDownloaded = modelManager.isModelDownloaded()
                    )
                }
            }
        }
    }
}
