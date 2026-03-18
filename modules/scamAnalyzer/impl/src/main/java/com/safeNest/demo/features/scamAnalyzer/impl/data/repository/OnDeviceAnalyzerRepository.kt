package com.safeNest.demo.features.scamAnalyzer.impl.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResultStatus
import com.safeNest.demo.features.scamAnalyzer.impl.data.extractor.MlKitOcrExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.data.utils.DeepResearchService
import com.safeNest.demo.features.scamAnalyzer.impl.data.utils.DeepResearchSummarizer
import com.safeNest.demo.features.scamAnalyzer.impl.data.utils.TextRedactor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor.EntityExtractor
import com.safeNest.demo.features.scamAnalyzer.impl.domain.repository.AnalyzerRepository
import javax.inject.Inject

class OnDeviceAnalyzerRepository @Inject constructor(
    private val entityExtractor: EntityExtractor,
) : AnalyzerRepository {

    companion object {
        private const val TAG = "OnDeviceAnalyzer"
    }

    override suspend fun analyze(input: AnalysisInput): AnalysisResult {
        Log.d(TAG, "Analyzing text: $input")
        when (input) {
            is AnalysisInput.Text -> {
                return analyzeText(input.text)
            }

            is AnalysisInput.Url -> {
                return analyzeUrl(input.url)
            }

            is AnalysisInput.Image -> {
                val text = ocr(input.uri, input.context)
                Log.d(TAG, "OCR text: $text")
                return analyzeText(text)
            }

            is AnalysisInput.Audio -> {
                val text = asr(input.uri)
                Log.d(TAG, "ASR text: $text")
                return analyzeText(text)
            }
        }
    }

    private suspend fun analyzeText(text: String) : AnalysisResult {
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


        // TODO: Classify with LLM
        // Call LLM Model, input: redactedText, summary
        Log.d(TAG, "Classifying with LLM")

        return AnalysisResult.Text(
            originalText = "",
            maskedText = "",
            status = AnalysisResultStatus.Unverified,
            analysisItems = null
        )
    }

    private fun analyzeUrl(url: String) : AnalysisResult {
        // TODO: Load webview
        // Call LLM Model, input: url, web content
        Log.d(TAG, "Classifying with LLM")

        return AnalysisResult.Text(
            originalText = "",
            maskedText = "",
            status = AnalysisResultStatus.Unverified,
            analysisItems = null
        )
    }

    private suspend fun ocr(uri: Uri, context: Context): String {
        return MlKitOcrExtractor.extractText(uri, context).getOrElse { "" }
    }

    private fun asr(uri: Uri) : String {
        // TODO: Call ASR
        return ""
    }
}