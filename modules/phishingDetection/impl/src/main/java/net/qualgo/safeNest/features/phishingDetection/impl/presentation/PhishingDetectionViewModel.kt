package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.WebsiteMetadata
import javax.inject.Inject

sealed class PhishingUiState {
    object Idle : PhishingUiState()
    object Loading : PhishingUiState()
    data class Downloading(val metadata: WebsiteMetadata, val progressPercent: Int) : PhishingUiState()
    data class Analyzing(val metadata: WebsiteMetadata, val partialAnalysis: String) : PhishingUiState()
    data class AnalysisComplete(val metadata: WebsiteMetadata, val analysis: String) : PhishingUiState()
    data class Error(val message: String) : PhishingUiState()
}

sealed class PhishingUiEffect {
    data class InspectUrl(val url: String) : PhishingUiEffect()
}

@HiltViewModel
class PhishingDetectionViewModel @Inject constructor(
    private val modelStorage: ModelStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhishingUiState>(PhishingUiState.Idle)
    val uiState: StateFlow<PhishingUiState> = _uiState

    private val _effect = Channel<PhishingUiEffect>(Channel.BUFFERED)
    val effect: Flow<PhishingUiEffect> = _effect.receiveAsFlow()

    private val analyzer = PhishingLlmAnalyzer()

    fun onScanRequested(url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) {
            _uiState.value = PhishingUiState.Error("Please enter a URL")
            return
        }
        val normalizedUrl = if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            trimmedUrl
        } else {
            "https://$trimmedUrl"
        }

        _uiState.value = PhishingUiState.Loading
        viewModelScope.launch {
            _effect.send(PhishingUiEffect.InspectUrl(normalizedUrl))
        }
    }

    fun onWebInspectionResult(url: String, result: Result<WebsiteMetadata>) {
        result.fold(
            onSuccess = { metadata -> startAnalysis(url, metadata) },
            onFailure = { error -> _uiState.value = PhishingUiState.Error(error.message ?: "Unknown error") }
        )
    }

    private fun startAnalysis(url: String, metadata: WebsiteMetadata) {
        viewModelScope.launch {
            try {
                val modelFolder = ModelDownloader.ensureModel(
                    modelDir = modelStorage.modelDir,
                    onProgress = { percent ->
                        _uiState.value = PhishingUiState.Downloading(metadata, percent)
                    },
                )

                withContext(Dispatchers.IO) {
                    analyzer.load(modelFolder)
                }

                _uiState.value = PhishingUiState.Analyzing(metadata, "")

                withContext(Dispatchers.IO) {
                    val tokens = StringBuilder()
                    analyzer.analyze(
                        url = url,
                        metadata = metadata,
                        onToken = { token ->
                            tokens.append(token)
                            _uiState.value = PhishingUiState.Analyzing(metadata, tokens.toString())
                        },
                        onDone = {
                            _uiState.value = PhishingUiState.AnalysisComplete(metadata, tokens.toString())
                        },
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PhishingUiState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        analyzer.release()
    }
}
