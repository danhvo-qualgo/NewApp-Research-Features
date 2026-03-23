package com.safeNest.demo.features.scamAnalyzer.impl.utils

import android.util.Log
import com.safeNest.demo.features.phishingDetection.impl.presentation.ModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class ModelState {
    object Idle : ModelState()
    data class Downloading(val progressPercent: Int) : ModelState()
    object Loading : ModelState()
    object Ready : ModelState()
    data class Error(val message: String) : ModelState()
}

/**
 * Singleton that owns the single [PhishingLlmAnalyzer] instance shared across all features.
 *
 * Call [ensureReady] once (e.g. from the option screen) to download and load the model.
 * Subsequent calls return immediately if the model is already [ModelState.Ready].
 * Both the URL checker and text/image checker VMs call [requireAnalyzer] to get the
 * already-loaded instance without re-downloading or re-loading.
 */
@Singleton
class ModelManager @Inject constructor(
    private val modelStorage: ModelStorage,
) {
    private val _state = MutableStateFlow<ModelState>(ModelState.Idle)
    val state: StateFlow<ModelState> = _state

    val analyzer = PhishingLlmAnalyzer()

    private val mutex = Mutex()

    /**
     * Ensures the model is downloaded and loaded. Safe to call concurrently — only one
     * download/load will run at a time thanks to the mutex.
     */
    suspend fun ensureReady(onProgress: (ModelState) -> Unit = {}) {
        if (_state.value is ModelState.Ready) return

        mutex.withLock {
            // Re-check inside lock in case another coroutine just finished
            if (_state.value is ModelState.Ready) return@withLock

            try {
                val modelFolder = ModelDownloader.ensureModel(
                    modelDir = modelStorage.modelDir,
                    onProgress = { percent ->
                        val s = ModelState.Downloading(percent)
                        _state.value = s
                        onProgress(s)
                    },
                )

                val loadingState = ModelState.Loading
                _state.value = loadingState
                onProgress(loadingState)

                analyzer.load(modelFolder)

                _state.value = ModelState.Ready
                onProgress(ModelState.Ready)
                Log.i(TAG, "Model ready")
            } catch (e: Exception) {
                val errorState = ModelState.Error(e.message ?: "Failed to load model")
                _state.value = errorState
                onProgress(errorState)
                Log.e(TAG, "Model load failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "ModelManager"
    }
}
