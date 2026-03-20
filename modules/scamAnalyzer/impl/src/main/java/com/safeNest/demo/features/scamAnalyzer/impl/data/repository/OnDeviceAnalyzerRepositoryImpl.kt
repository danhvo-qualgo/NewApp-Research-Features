package com.safeNest.demo.features.scamAnalyzer.impl.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisItem
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResultType
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisStatus
import com.safeNest.demo.features.scamAnalyzer.impl.data.extractor.MlKitOcrExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.data.utils.DeepResearchService
import com.safeNest.demo.features.scamAnalyzer.impl.data.utils.DeepResearchSummarizer
import com.safeNest.demo.features.scamAnalyzer.impl.data.utils.TextRedactor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor.EntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import com.safeNest.demo.features.scamAnalyzer.impl.utils.ModelManager
import com.safeNest.demo.features.scamAnalyzer.impl.utils.asr.WhisperModelManager
import com.safeNest.demo.features.scamAnalyzer.impl.utils.asr.WhisperTranscriber
import com.uney.core.network.api.models.ApiResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceAnalyzerRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val entityExtractor: EntityExtractor,
    private val modelManager: ModelManager,
    private val whisperModelManager: WhisperModelManager,
) : AnalyzerRepository {
    companion object {
        private const val TAG = "OnDeviceAnalyzer"
        private fun getPrompt(msg: String, summary: String): String {
            return """
        Classify this message as a cybersecurity threat.

        Status codes:
        0=Safe, 1=Scam, 2=Unverified

        Safe: No phishing/scam indicators.
        Scam: Clear signals — impersonation, urgency, fake links, threats, sensitive info requests.
        Unverified: Suspicious but insufficient evidence.

        Rules:
        - Return ONLY valid JSON, no markdown.
        - reasons MUST be [] when status=0.

        {"status":0|1|2,"reasons":[]}

        MESSAGE: $msg
        CONTEXT: $summary
    """.trimIndent()
        }
    }


    @Serializable
    data class ModelResult(
        val status: Int,
        val reasons: List<Reason>
    ) {
        fun getStatus() = when (status) {
            0 -> AnalysisStatus.Safe
            1 -> AnalysisStatus.Scam
            else -> AnalysisStatus.Unverified
        }
    }

    @Serializable
    data class Reason(
        val title: String,
        val description: String,
    )

    private suspend fun analyzeText(text: String): Pair<String, ModelResult> {
        // Extract entities
        val extractedEntities = entityExtractor.extract(text)
        Log.d(TAG, "Extracted entities: $extractedEntities")

        // Text redaction
        val redactedText = TextRedactor.redact(text, extractedEntities)
        Log.d(TAG, "Redacted text: $redactedText")

        // Deep research
        val deepResearchResult = DeepResearchService.research(extractedEntities)
        Log.d(TAG, "Deep research result: $deepResearchResult")

        // Summarize
        val summary = DeepResearchSummarizer.summarize(text, deepResearchResult)
        Log.d(TAG, "Summary: $summary")

        modelManager.ensureReady()
        val result = modelManager.analyzer
            .llmProcessing(getPrompt(text, summary))
            .toList()
            .joinToString("")

        val thinkRegex = Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL)
        val cleaned = result.replace(thinkRegex, "")

        val jsonRegex = Regex("\\{.*\\}", RegexOption.DOT_MATCHES_ALL)
        val json = jsonRegex.find(cleaned)?.value.orEmpty()

        Log.d(TAG, "Model result: $result")
        Log.d(TAG, "Json result: $json")
        val modelResult = Json.decodeFromString<ModelResult>(json)

        return Pair(redactedText, modelResult)
    }

    override suspend fun analyzeAudio(uri: Uri): ApiResult<AnalysisResult> {
        whisperModelManager.ensureReady()

        val text = WhisperTranscriber.transcribe(
            uri,
            context,
            whisperModelManager.interpreter,
            whisperModelManager.modelDir
        )

        val (_, result) = analyzeText(text)
        return ApiResult.Success(
            AnalysisResult(
                data = AnalysisResultType.Audio(uri.toString()),
                status = result.getStatus(),
                keyFindings = result.reasons.map {
                    AnalysisItem(
                        title = it.title,
                        description = it.description
                    )
                }
            )
        )
    }

    override suspend fun analyzeUrl(url: String): ApiResult<AnalysisResult> {
        // TODO: Load webview
        // Call LLM Model, input: url, web content
        Log.d(TAG, "Classifying with LLM")

        return ApiResult.Exception(Throwable())
    }

    override suspend fun analyzeText(
        bundleId: String,
        sender: String,
        text: String
    ): ApiResult<AnalysisResult> {
        val (redacted, result) = analyzeText(text)

        return ApiResult.Success(
            AnalysisResult(
                data = AnalysisResultType.Text(text, redacted),
                status = result.getStatus(),
                keyFindings = result.reasons.map {
                    AnalysisItem(
                        title = it.title,
                        description = it.description
                    )
                }
            )
        )
    }

    override suspend fun analyzeImage(uri: Uri): ApiResult<AnalysisResult> {
        val text = MlKitOcrExtractor.extractText(uri, context).getOrElse { "" }

        val (_, result) = analyzeText(text)
        return ApiResult.Success(
            AnalysisResult(
                data = AnalysisResultType.Image(uri.toString(), text.isEmpty()),
                status = result.getStatus(),
                keyFindings = result.reasons.map {
                    AnalysisItem(
                        title = it.title,
                        description = it.description
                    )
                }
            )
        )
    }
}