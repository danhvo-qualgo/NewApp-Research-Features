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
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.ModelManager
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr.WhisperModelManager
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr.WhisperTranscriber
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities
import javax.inject.Inject

@HiltViewModel
class TextImageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelManager: ModelManager,
    private val whisperModelManager: WhisperModelManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TextImageUiState>(TextImageUiState.Idle)
    val uiState: StateFlow<TextImageUiState> = _uiState
    private val regexExtractor = RegexEntityExtractor()

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

    fun onAudioSelected(uri: Uri, method: ExtractionMethod) {
        viewModelScope.launch {
            // File size check — reject files larger than 10 MB
            val fileSizeBytes = runCatching {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
            }.getOrDefault(0L)
            if (fileSizeBytes > 10 * 1024 * 1024) {
                _uiState.value = TextImageUiState.Error("Audio file too large (max 10 MB)")
                return@launch
            }

            _uiState.value = TextImageUiState.TranscribingAudio

            try {
                whisperModelManager.ensureReady()

                val transcribed = WhisperTranscriber.transcribe(
                    uri = uri,
                    context = context,
                    interpreter = whisperModelManager.interpreter,
                    modelDir = whisperModelManager.modelDir,
                )

                if (transcribed.isBlank()) {
                    _uiState.value = TextImageUiState.Error("Could not transcribe audio — no speech detected")
                } else {
                    runExtraction(transcribed, method)
                }
            } catch (e: Exception) {
                Log.e("TextImageViewModel", "Audio transcription failed", e)
                _uiState.value = TextImageUiState.Error(
                    "Transcription failed: ${e.message ?: "Unknown error"}"
                )
            }
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
                        modelManager.ensureReady()
                        val entities = LlmEntityExtractor.extract(
                            text = text,
                            analyzer = modelManager.analyzer,
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

            val summary = DeepResearchSummarizer.summarize(redactedText, researchResult)
            val analysisPrompt = buildAnalysisPrompt(redactedText, summary)

            modelManager.ensureReady()

            val tokens = StringBuilder()
            withContext(Dispatchers.IO) {
                modelManager.analyzer.llmProcessing(
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

    private fun buildAnalysisPrompt(text: String, summary: String): String {
        val prompt = """
            <|im_start|>system
            </no_think>
            You are a scam detection system analyzing Vietnamese SMS messages.
            Your task: classify the message and explain your reasoning.
            <|im_end|>
            
            <|im_start|>user
            == MESSAGE (PII redacted) ==
            $text
            
            == SECURITY SIGNALS ==
            $summary
            
            == INSTRUCTIONS ==
            Based on the message content and security signals above, classify this message into exactly one category:
            - SAFE: Legitimate message — OTP codes from banks/services, delivery notifications, genuine service alerts, promotional marketing from known brands
            - SCAM: Harmful or deceptive message — phishing, credential theft, fake prizes, loan fraud, impersonation, urgent account warnings with suspicious links
            - UNSURE: Cannot determine with reasonable confidence
            
            Key guidance:
            - An OTP message that says "do not share this code" is SAFE — it is a bank delivering a code to YOU, not asking you to reveal it.
            - Delivery notifications with tracking numbers and shipper info are SAFE.
            - Marketing messages with promotional discounts from known brands (e.g., thegioididong.com) are SAFE.
            
            Respond in this exact format:
            VERDICT: [SAFE|SCAM|UNSURE]
            CONFIDENCE: [HIGH|MEDIUM|LOW]
            REASONING: [1-3 sentences explaining why]
            <|im_end|>
            
            <|im_start|>assistant
            """.trimIndent()
        return prompt
    }
}
