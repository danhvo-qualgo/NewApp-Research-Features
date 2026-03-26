/*
 * AudioAnalyzer.kt — Audio STT + diarization + scam analysis.
 *
 * Uses Android SpeechRecognizer for transcription.
 * MFCC + K-means for 2-speaker diarization.
 * Passes transcript to LMClient with audio-specific prompt.
 */
package com.safenest.urlanalyzer.audio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import com.safenest.urlanalyzer.shared.LMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

data class AudioAnalysisResult(
    val verdict: String,
    val riskScore: Double,
    val keyFindings: List<AudioKeyFinding>,
    val transcript: String,
    val speakerCount: Int,
    val rawResponse: String,
    val elapsed: Double
)

data class AudioKeyFinding(
    val category: String,
    val description: String
)

class AudioAnalyzer(
    private val lmClient: LMClient,
    context: Context
) {
    private val systemPrompt: String

    init {
        val json = context.assets.open("config/audio_prompts.json")
            .bufferedReader().use { it.readText() }
        val config = JSONObject(json)
        systemPrompt = config.getString("system_prompt")
    }

    suspend fun analyze(uri: Uri, context: Context): AudioAnalysisResult {
        val start = System.nanoTime()

        // 1. Transcribe using SpeechRecognizer
        val rawTranscript = transcribe(uri, context)
        if (rawTranscript.isBlank()) {
            return AudioAnalysisResult(
                "unknown", 0.5, emptyList(), "(no speech detected)", 0,
                "", (System.nanoTime() - start) / 1e9
            )
        }

        // 2. For now: simple transcript (no diarization via SpeechRecognizer)
        // Android SpeechRecognizer doesn't provide timestamps per-word reliably
        // MFCC diarization would require reading raw audio — future enhancement
        val transcript = rawTranscript

        // 3. LLM analysis
        lmClient.load(systemPrompt)
        val rawJson = lmClient.complete(transcript)
        val elapsed = (System.nanoTime() - start) / 1e9

        return parseResult(rawJson, transcript, elapsed)
    }

    private suspend fun transcribe(uri: Uri, context: Context): String {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return "(speech recognition not available)"
        }

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    putExtra("android.speech.extra.AUDIO_SOURCE", uri.toString())
                }

                var resumed = false
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        if (resumed) return
                        resumed = true
                        val matches = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        val transcript = matches?.firstOrNull() ?: ""
                        recognizer.destroy()
                        cont.resume(transcript)
                    }

                    override fun onError(error: Int) {
                        if (resumed) return
                        resumed = true
                        recognizer.destroy()
                        cont.resume("(recognition error: $error)")
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                recognizer.startListening(intent)

                cont.invokeOnCancellation {
                    recognizer.cancel()
                    recognizer.destroy()
                }
            }
        }
    }

    private fun parseResult(rawJson: String, transcript: String, elapsed: Double): AudioAnalysisResult {
        return try {
            val obj = JSONObject(rawJson)
            val findings = mutableListOf<AudioKeyFinding>()
            val kfArray = obj.optJSONArray("key_findings") ?: JSONArray()
            for (i in 0 until kfArray.length()) {
                val kf = kfArray.getJSONObject(i)
                findings.add(AudioKeyFinding(
                    category = kf.optString("category", ""),
                    description = kf.optString("description", "").take(200)
                ))
            }
            AudioAnalysisResult(
                verdict = obj.optString("verdict", "unknown"),
                riskScore = obj.optDouble("risk_score", 0.5).coerceIn(0.0, 1.0),
                keyFindings = findings,
                transcript = transcript,
                speakerCount = 0,
                rawResponse = rawJson, elapsed = elapsed
            )
        } catch (e: Exception) {
            AudioAnalysisResult("unknown", 0.5, emptyList(), transcript, 0, rawJson, elapsed)
        }
    }
}
