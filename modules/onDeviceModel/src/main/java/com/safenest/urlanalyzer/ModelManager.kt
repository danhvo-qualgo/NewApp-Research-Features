package com.safenest.urlanalyzer

import android.content.Context
import android.util.Log
import com.safenest.urlanalyzer.audio.AudioAnalyzer
import com.safenest.urlanalyzer.image.ImageAnalyzer
import com.safenest.urlanalyzer.shared.LMClient
import com.safenest.urlanalyzer.shared.LlamaEngine
import com.safenest.urlanalyzer.text.SMSAnalyzerOrchestrator
import com.safenest.urlanalyzer.text.TextAnalyzerClassifier
import com.safenest.urlanalyzer.url.URLAnalyzerOrchestrator
import com.safenest.urlanalyzer.url.gate1.Gate1Classifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelManager @Inject constructor(
    @param:ApplicationContext private val ctx: Context,
    private val downloader: ModelDownloader,
    private val config: ModelConfig,
) {
    private val mutex = Mutex()

    private val _state = MutableStateFlow<ModelState>(ModelState.Uninitialized)

    /** Observe this to react to download progress and readiness. */
    val state: StateFlow<ModelState> = _state.asStateFlow()

    /** Convenience accessor; null unless state is [ModelState.Ready]. */
    val components: ModelComponents?
        get() = (_state.value as? ModelState.Ready)?.components

    // ── public API ───────────────────────────────────────────────────────────

    fun isModelDownloaded(): Boolean = downloader.isDownloaded()

    /**
     * Downloads (if needed) and initialises the model and all analysis components.
     * Safe to call concurrently — subsequent callers wait on the [Mutex] and return
     * immediately once the first caller's work is complete.
     */
    suspend fun initialize() {
        mutex.withLock {
            if (_state.value is ModelState.Ready) return@withLock

            try {
                val modelFile = downloader.ensureDownloaded { progress ->
                    _state.value = ModelState.Loading(progress)
                }

                _state.value = ModelState.Loading(100)

                val components = buildComponents(modelFile.absolutePath)
                _state.value = ModelState.Ready(components)

                Log.d(TAG, "Initialization complete")
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                _state.value = ModelState.Error(e)
                throw e
            }
        }
    }

    /**
     * Releases native engine state, tears down all components, deletes the model
     * file, and resets [state] to [ModelState.Uninitialized].
     * Safe to call concurrently with [initialize] — the [Mutex] prevents interleaving.
     */
    suspend fun delete() {
        mutex.withLock {
            val current = _state.value
            if (current is ModelState.Ready) {
                releaseComponents(current.components)
            }

            downloader.deleteFile()
            _state.value = ModelState.Uninitialized

            Log.d(TAG, "Model deleted and state reset")
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private suspend fun buildComponents(modelPath: String): ModelComponents {
        val engine = LlamaEngine(
            modelPath = modelPath,
            nCtx = config.nCtx,
            nGpuLayers = config.nGpuLayers,
            maxTokens = config.maxTokens,
        )

        if (!engine.ensureInit()) {
            throw RuntimeException("Failed to load model at $modelPath")
        }

        val lmClient = LMClient(engine, maxTokens = config.maxTokens)

        val gate1 = Gate1Classifier(ctx)

        val gate2Prompt = ctx.assets.open("config/gate2_prompts.json")
            .bufferedReader()
            .use { it.readText() }
            .let { JSONObject(it).getString("system_prompt") }

        val gate2 = URLAnalyzerOrchestrator(gate1, lmClient, gate2Prompt)

        val textClassifier = TextAnalyzerClassifier(lmClient, ctx)
        val smsClassifier = SMSAnalyzerOrchestrator(textClassifier, gate1)

        val imageAnalyzer = ImageAnalyzer(lmClient, ctx)
        val audioAnalyzer = AudioAnalyzer(lmClient, ctx)

        return ModelComponents(
            engine = engine,
            lmClient = lmClient,
            gate1 = gate1,
            gate2 = gate2,
            smsClassifier = smsClassifier,
            imageAnalyzer = imageAnalyzer,
            audioAnalyzer = audioAnalyzer,
        )
    }

    private suspend fun releaseComponents(components: ModelComponents) {
        try {
            components.engine.release()
        } catch (e: Exception) {
            Log.e(TAG, "Engine release failed", e)
        }
    }

    private companion object {
        const val TAG = "ModelManager"
    }
}