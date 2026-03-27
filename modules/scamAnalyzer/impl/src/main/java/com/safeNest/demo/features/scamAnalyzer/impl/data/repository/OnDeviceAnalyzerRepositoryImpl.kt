package com.safeNest.demo.features.scamAnalyzer.impl.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisItem
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResultType
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisStatus
import com.safeNest.demo.features.scamAnalyzer.impl.data.extractor.MlKitOcrExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.data.utils.TextRedactor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor.EntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import com.safeNest.demo.features.scamAnalyzer.impl.utils.asr.WhisperModelManager
import com.safeNest.demo.features.scamAnalyzer.impl.utils.asr.WhisperTranscriber
import com.safenest.urlanalyzer.ModelManager
import com.uney.core.network.api.models.ApiResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceAnalyzerRepositoryImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val entityExtractor: EntityExtractor,
    private val modelManager: ModelManager,
    private val whisperModelManager: WhisperModelManager,
) : AnalyzerRepository {
    companion object {
        private const val TAG = "OnDeviceAnalyzer"
    }


    @Serializable
    data class ModelResult(
        val category: AnalysisStatus,
        val reasons: List<Reason>
    ) {
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

        modelManager.initialize()
        val smsClassifier = modelManager.components!!.smsClassifier
        val data = smsClassifier.analyze(text)

        return Pair(
            redactedText, ModelResult(
                category = when (data.verdict.lowercase()) {
                    "scam" -> AnalysisStatus.Scam
                    "safe" -> AnalysisStatus.Safe
                    else -> AnalysisStatus.Unverified
                },
                reasons = data.keyFindings.map {
                    Reason(
                        title = it.category,
                        description = it.description
                    )
                }
            )
        )
    }

    private fun String.toStatus() = when (this) {
        "scam" -> AnalysisStatus.Scam
        "safe" -> AnalysisStatus.Safe
        else -> AnalysisStatus.Unverified
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
                status = result.category,
                keyFindings = result.reasons.map {
                    AnalysisItem(
                        title = it.title,
                        description = it.description
                    )
                }
            )
        )


//        modelManager.initialize()
//        val audioAnalyzer = modelManager.audioAnalyzer!!
//
//        val result = audioAnalyzer.analyze(uri, context)
//        return ApiResult.Success(
//            AnalysisResult(
//                data = AnalysisResultType.Audio(uri.toString()),
//                status = result.verdict.toStatus(),
//                keyFindings = result.keyFindings.map {
//                    AnalysisItem(
//                        title = it.category,
//                        description = it.description
//                    )
//                }
//            )
//        )
    }

    override suspend fun analyzeUrl(url: String): ApiResult<AnalysisResult> {
        modelManager.initialize()
        val gate2 = modelManager.components!!.gate2

        val result = gate2.analyze(url)

        return ApiResult.Success(
            AnalysisResult(
                data = AnalysisResultType.Url(url),
                status = result.verdict.toStatus(),
                keyFindings = result.keyFindings.map {
                    AnalysisItem(
                        title = it.category,
                        description = it.description
                    )
                }
            )
        )
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
                status = result.category,
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
                status = result.category,
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