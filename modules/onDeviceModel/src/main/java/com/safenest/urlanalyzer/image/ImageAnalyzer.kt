/*
 * ImageAnalyzer.kt — OCR + chat diarization + scam analysis.
 *
 * Uses ML Kit Text Recognition for OCR.
 * Detects chat layout from bounding box positions (left/right bubble heuristic).
 * Passes result to LMClient with appropriate prompt.
 */
package com.safenest.urlanalyzer.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.safenest.urlanalyzer.shared.LMClient
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ImageAnalysisResult(
    val verdict: String,
    val riskScore: Double,
    val keyFindings: List<ImageKeyFinding>,
    val ocrText: String,
    val isChat: Boolean,
    val rawResponse: String,
    val elapsed: Double
)

data class ImageKeyFinding(
    val category: String,
    val description: String
)

data class OCRBlock(
    val text: String,
    val centerX: Float,
    val centerY: Float
)

class ImageAnalyzer(
    private val lmClient: LMClient,
    context: Context
) {
    private val chatSystemPrompt: String
    private val screenshotSystemPrompt: String

    // Noise filter patterns (timestamps, date headers)
    private val TIMESTAMP_REGEX = Regex("""^\d{1,2}:\d{2}(\s*[APap][Mm])?$""")
    private val DATE_HEADER_REGEX = Regex(
        """^(Today|Yesterday|Hôm nay|Hôm qua|Thứ\s+\w+|Chủ\s+Nhật|\d{1,2}/\d{1,2}(/\d{2,4})?|\w{3}\s+\d{1,2})$""",
        RegexOption.IGNORE_CASE
    )

    init {
        val json = context.assets.open("config/ocr_prompts.json")
            .bufferedReader().use { it.readText() }
        val config = JSONObject(json)
        chatSystemPrompt = config.getString("chat_system_prompt")
        screenshotSystemPrompt = config.getString("screenshot_system_prompt")
    }

    suspend fun analyze(uri: Uri, context: Context): ImageAnalysisResult {
        val start = System.nanoTime()

        // 1. Load bitmap
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: throw IllegalArgumentException("Cannot read image")

        // 2. OCR
        val blocks = performOCR(bitmap)

        // 3. Chat detection + diarization
        val isChat = detectChat(blocks)
        val ocrText = if (isChat) {
            diarize(blocks)
        } else {
            blocks.sortedBy { it.centerY }
                .map { it.text }
                .filter { !isNoise(it) }
                .joinToString("\n")
        }

        // 4. LLM analysis
        val systemPrompt = if (isChat) chatSystemPrompt else screenshotSystemPrompt
        lmClient.load(systemPrompt)
        val rawJson = lmClient.complete(ocrText)
        val elapsed = (System.nanoTime() - start) / 1_000_000_000.0

        return parseResult(rawJson, ocrText, isChat, elapsed)
    }

    private suspend fun performOCR(bitmap: Bitmap): List<OCRBlock> =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val blocks = result.textBlocks.flatMap { block ->
                        block.lines.mapNotNull { line ->
                            val box = line.boundingBox ?: return@mapNotNull null
                            val cx = (box.left + box.right) / 2f / bitmap.width
                            val cy = (box.top + box.bottom) / 2f / bitmap.height
                            OCRBlock(line.text, cx, cy)
                        }
                    }
                    cont.resume(blocks)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    private fun detectChat(blocks: List<OCRBlock>): Boolean {
        if (blocks.size < 4) return false
        val leftCount = blocks.count { it.centerX < 0.4f }
        val rightCount = blocks.count { it.centerX > 0.55f }
        return leftCount >= 2 && rightCount >= 2
    }

    private fun diarize(blocks: List<OCRBlock>): String {
        return blocks
            .sortedBy { it.centerY }
            .filter { !isNoise(it.text) }
            .joinToString("\n") { block ->
                val speaker = if (block.centerX < 0.45f) "Sender" else "Me"
                "$speaker: ${block.text}"
            }
    }

    private fun isNoise(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 2) return true
        if (TIMESTAMP_REGEX.matches(trimmed)) return true
        if (DATE_HEADER_REGEX.matches(trimmed)) return true
        return false
    }

    private fun parseResult(
        rawJson: String, ocrText: String, isChat: Boolean, elapsed: Double
    ): ImageAnalysisResult {
        return try {
            val obj = JSONObject(rawJson)
            val findings = mutableListOf<ImageKeyFinding>()
            val kfArray = obj.optJSONArray("key_findings") ?: JSONArray()
            for (i in 0 until kfArray.length()) {
                val kf = kfArray.getJSONObject(i)
                findings.add(ImageKeyFinding(
                    category = kf.optString("category", ""),
                    description = kf.optString("description", "").take(200)
                ))
            }
            ImageAnalysisResult(
                verdict = obj.optString("verdict", "unknown"),
                riskScore = obj.optDouble("risk_score", 0.5).coerceIn(0.0, 1.0),
                keyFindings = findings,
                ocrText = ocrText, isChat = isChat,
                rawResponse = rawJson, elapsed = elapsed
            )
        } catch (e: Exception) {
            ImageAnalysisResult("unknown", 0.5, emptyList(), ocrText, isChat, rawJson, elapsed)
        }
    }
}
