package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.ModelDownloader
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.ModelStorage
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.PhishingLlmAnalyzer
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities
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
                    runDeepAnalysis(text, entities)
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
                        runDeepAnalysis(text, entities)
                    } catch (e: Exception) {
                        _uiState.value = TextImageUiState.Error(
                            e.message ?: "LLM extraction failed"
                        )
                    }
                }
            }
        }
    }

    private suspend fun runDeepAnalysis(sourceText: String, entities: ExtractedEntities) {
        _uiState.value = TextImageUiState.DeepAnalyzing(sourceText, entities)
        try {
            val (redactedText, researchResult) = coroutineScope {
                val redactedDeferred = async(Dispatchers.Default) {
                    TextRedactor.redact(sourceText, entities)
                }
                val researchDeferred = async(Dispatchers.IO) {
                    DeepResearchService.research(entities)
                }
                Pair(redactedDeferred.await(), researchDeferred.await())
            }

            val summary = DeepResearchSummarizer.summarize(researchResult)
            val analysisPrompt = buildAnalysisPrompt(redactedText, summary)

            val tokens = StringBuilder()
            withContext(Dispatchers.IO) {
                llmAnalyzer.llmProcessing(
                    prompt = analysisPrompt,
                    onToken = { token ->
                        tokens.append(token)
                        _uiState.value = TextImageUiState.DeepAnalyzing(
                            sourceText = sourceText,
                            entities = entities,
                            partialOutput = tokens.toString(),
                        )
                    },
                    onDone = {},
                )
            }

            _uiState.value = TextImageUiState.AnalysisComplete(
                sourceText = sourceText,
                entities = entities,
                redactedText = redactedText,
                summary = summary,
                analysis = tokens.toString(),
            )
        } catch (e: Exception) {
            _uiState.value = TextImageUiState.Error(
                e.message ?: "Deep analysis failed"
            )
        }
    }

    private fun buildAnalysisPrompt(redactedText: String, summary: String): String = buildString {
        append("<|im_start|>system\n")
        append("You are a cybersecurity expert specializing in phishing and scam detection. ")
        append("Analyze the following message for phishing risk based on the redacted content and research findings.")
        append("<|im_end|>\n")
        append("<|im_start|>user\n")
        append("Message (sensitive data redacted):\n$redactedText\n\n")
        if (summary.isNotBlank()) {
            append("Research findings:\n$summary\n\n")
        }
        append("Assess the phishing risk. Respond text only with:: Risk level (Likely Scam | Suspicious | Likely Legit | Unknown), Confidence (0% to 100%), and 2-3 key signals.")
        append("<|im_end|>\n")
        append("<|im_start|>assistant\n")
    }

    override fun onCleared() {
        super.onCleared()
        llmAnalyzer.release()
    }
}
