package com.safeNest.demo.features.scamAnalyzer.impl.utils.urlChecker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.impl.utils.models.WebsiteMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.safeNest.demo.features.scamAnalyzer.impl.utils.ModelManager
import javax.inject.Inject

sealed class PhishingUiState {
    object Idle : PhishingUiState()
    object Loading : PhishingUiState()
    data class Downloading(val metadata: WebsiteMetadata, val progressPercent: Int) :
        PhishingUiState()

    data class Analyzing(val metadata: WebsiteMetadata, val partialAnalysis: String) :
        PhishingUiState()

    data class AnalysisComplete(val metadata: WebsiteMetadata, val analysis: String) :
        PhishingUiState()

    data class Error(val message: String) : PhishingUiState()
}

sealed class PhishingUiEffect {
    data class InspectUrl(val url: String) : PhishingUiEffect()
}

@HiltViewModel
class PhishingDetectionViewModel @Inject constructor(
    private val modelManager: ModelManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhishingUiState>(PhishingUiState.Idle)
    val uiState: StateFlow<PhishingUiState> = _uiState

    private val _effect = Channel<PhishingUiEffect>(Channel.BUFFERED)
    val effect: Flow<PhishingUiEffect> = _effect.receiveAsFlow()

    fun onScanRequested(url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) {
            _uiState.value = PhishingUiState.Error("Please enter a URL")
            return
        }
        val normalizedUrl =
            if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
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
            onFailure = { error ->
                _uiState.value = PhishingUiState.Error(error.message ?: "Unknown error")
            }
        )
    }

    private fun startAnalysis(url: String, metadata: WebsiteMetadata) {
        viewModelScope.launch {
            try {
                // Model is already loaded by the option screen; ensureReady() is a no-op
                // if already Ready, so this acts as a safe fallback only.
                modelManager.ensureReady()

                _uiState.value = PhishingUiState.Analyzing(metadata, "")

                withContext(Dispatchers.IO) {
                    val tokens = StringBuilder()
                    modelManager.analyzer.llmProcessing(
                        prompt = buildPrompt(url, metadata),
                        onToken = { token ->
                            tokens.append(token)
                            _uiState.value = PhishingUiState.Analyzing(metadata, tokens.toString())
                        },
                        onDone = {
                            _uiState.value =
                                PhishingUiState.AnalysisComplete(metadata, tokens.toString())
                        },
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PhishingUiState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    private fun buildPrompt(url: String, metadata: WebsiteMetadata): String {
        val body = metadata.bodyText.take(400)
        val description = metadata.ogDescription.ifBlank { metadata.description }

        return """
            <|im_start|>system
            You are a cybersecurity expert specializing in phishing detection. Analyze the website information and its body text and give a concise risk assessment.
            <|im_end|>
            <|im_start|>user
            Analyze this website for phishing risk:
            
            URL: $url
            ${metadata.title.takeIf { it.isNotBlank() }?.let { "Title: $it" } ?: ""}
            ${description.takeIf { it.isNotBlank() }?.let { "Description: $it" } ?: ""}
            ${body.takeIf { it.isNotBlank() }?.let { "Body text (take 400 first): $it" } ?: ""}
            
            Respond text only with: Risk level (Likely Scam | Suspicious | Likely Legit | Unknown), Confidence (0% to 100%), and 2-3 key signals.
            <|im_end|>
            <|im_start|>assistant
            """.trimIndent()
    }
}
