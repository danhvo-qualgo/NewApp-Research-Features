package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import android.app.Application
import android.view.ViewGroup
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

@HiltViewModel
class PhishingDetectionViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<PhishingUiState>(PhishingUiState.Idle)
    val uiState: StateFlow<PhishingUiState> = _uiState

    private var inspector: WebsiteInspectorWebView? = null
    private val analyzer = PhishingLlmAnalyzer()

    fun scanUrl(url: String, container: ViewGroup) {
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

        inspector?.cleanup()
        _uiState.value = PhishingUiState.Loading

        val newInspector = WebsiteInspectorWebView(getApplication(), container)
        inspector = newInspector
        newInspector.inspect(normalizedUrl) { scanResult: kotlin.Result<WebsiteMetadata> ->
            scanResult.fold(
                onSuccess = { metadata -> startAnalysis(normalizedUrl, metadata) },
                onFailure = { error -> _uiState.value = PhishingUiState.Error(error.message ?: "Unknown error") }
            )
        }
    }

    private fun startAnalysis(url: String, metadata: WebsiteMetadata) {
        viewModelScope.launch {
            try {
                val modelFolder = ModelDownloader.ensureModel(
                    context = getApplication(),
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
        inspector?.cleanup()
        inspector = null
        analyzer.release()
    }
}
