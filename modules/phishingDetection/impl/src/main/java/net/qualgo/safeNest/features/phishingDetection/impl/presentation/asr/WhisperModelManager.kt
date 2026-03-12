package net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

sealed class WhisperModelState {
    object Idle : WhisperModelState()
    data class Downloading(val progressPercent: Int) : WhisperModelState()
    object Loading : WhisperModelState()
    object Ready : WhisperModelState()
    data class Error(val message: String) : WhisperModelState()
}

/**
 * Singleton that owns the single TFLite [Interpreter] instance for Whisper.
 *
 * Call [ensureReady] once (e.g. from OptionViewModel) to download and load the model.
 * Subsequent calls return immediately if state is already [WhisperModelState.Ready].
 */
@Singleton
class WhisperModelManager @Inject constructor(
    private val modelStorage: WhisperModelStorage,
) {
    private val _state = MutableStateFlow<WhisperModelState>(WhisperModelState.Idle)
    val state: StateFlow<WhisperModelState> = _state

    private var _interpreter: Interpreter? = null
    val interpreter: Interpreter
        get() = _interpreter ?: error("WhisperModelManager: interpreter not ready — call ensureReady() first")

    val modelDir: File
        get() = modelStorage.modelFile.parentFile!!

    private val mutex = Mutex()

    suspend fun ensureReady() {
        if (_state.value is WhisperModelState.Ready) return

        mutex.withLock {
            if (_state.value is WhisperModelState.Ready) return@withLock

            try {
                val modelFile = WhisperDownloader.ensureModel(
                    modelDir = modelDir,
                    onProgress = { percent ->
                        _state.value = WhisperModelState.Downloading(percent)
                    },
                )

                _state.value = WhisperModelState.Loading

                withContext(Dispatchers.IO) {
                    val buffer = loadModelBuffer(modelFile.absolutePath)
                    val flexDelegate = FlexDelegate()
                    val options = Interpreter.Options().apply {
                        numThreads = 4
                        addDelegate(flexDelegate)
                    }
                    _interpreter = Interpreter(buffer, options)
                }

                _state.value = WhisperModelState.Ready
                Log.i(TAG, "Whisper model ready")
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to load Whisper model"
                _state.value = WhisperModelState.Error(msg)
                Log.e(TAG, "Whisper model load failed", e)
            }
        }
    }

    private fun loadModelBuffer(path: String): MappedByteBuffer {
        val fis = FileInputStream(path)
        val channel = fis.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
    }

    companion object {
        private const val TAG = "WhisperModelManager"
    }
}
