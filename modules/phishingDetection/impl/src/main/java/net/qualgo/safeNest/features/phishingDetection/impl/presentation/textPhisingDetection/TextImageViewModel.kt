package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.ModelDownloader
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.ModelStorage
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.PhishingLlmAnalyzer
import javax.inject.Inject

@HiltViewModel
class TextImageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelStorage: ModelStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TextImageUiState>(TextImageUiState.Idle)
    val uiState: StateFlow<TextImageUiState> = _uiState
    private val regexExtractor = RegexEntityExtractor()
    private val llmAnalyzer = PhishingLlmAnalyzer()

    fun onTextSubmit(text: String, method: ExtractionMethod) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            _uiState.value = TextImageUiState.Error("Please enter some text")
            return
        }
        runExtraction(trimmed, method)
    }

    fun onImageSelected(uri: Uri, method: ExtractionMethod) {
        viewModelScope.launch {
            _uiState.value = TextImageUiState.OcrRunning
            val ocrResult = MlKitOcrExtractor.extractText(uri, context)
            ocrResult.fold(
                onSuccess = { extractedText ->
                    if (extractedText.isBlank()) {
                        _uiState.value = TextImageUiState.Error("No text found in the image")
                    } else {
                        runExtraction(extractedText, method)
                    }
                },
                onFailure = { error ->
                    _uiState.value = TextImageUiState.Error(
                        "OCR failed: ${error.message ?: "Unknown error"}"
                    )
                }
            )
        }
    }

    private fun runExtraction(text: String, method: ExtractionMethod) {
        viewModelScope.launch {
            when (method) {
                ExtractionMethod.REGEX -> {
                    _uiState.value = TextImageUiState.Extracting(method, text)
                    val entities = withContext(Dispatchers.Default) {
                        regexExtractor.extract(text)
                    }
                    _uiState.value = TextImageUiState.Done(text, entities)
                }

                ExtractionMethod.LLM -> {
                    Log.d("LLM extraction", text)
                    _uiState.value = TextImageUiState.Extracting(method, text)
                    try {
                        val modelFolder = ModelDownloader.ensureModel(
                            modelDir = modelStorage.modelDir,
                            onProgress = { /* model already cached for URL checker usage */ },
                        )
                        withContext(Dispatchers.IO) {
                            llmAnalyzer.load(modelFolder)
                        }
                        val entities = LlmEntityExtractor.extract(
                            text = text,
                            analyzer = llmAnalyzer,
                            onProgress = { partial ->
                                _uiState.value = TextImageUiState.Extracting(method, text, partial)
                            },
                        )
                        _uiState.value = TextImageUiState.Done(text, entities)
                    } catch (e: Exception) {
                        _uiState.value = TextImageUiState.Error(
                            e.message ?: "LLM extraction failed"
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmAnalyzer.release()
    }
}
