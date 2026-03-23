package com.safeNest.demo.features.scamAnalyzer.impl.utils

import android.util.Log
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

/**
 * Kotlin facade over the native MNN LLM via [libmnnllmphishing.so].
 *
 * Lifecycle:
 * 1. Call [load] on [kotlinx.coroutines.Dispatchers.IO] to initialise the model.
 * 2. Call [] (also on IO) — streaming tokens arrive via [onToken]; [onDone] fires last.
 * 3. Call [release] in [androidx.lifecycle.ViewModel.onCleared] to free native memory.
 *
 * The [ProgressListener] interface mirrors the JNI callback expected by [llm_jni.cpp]:
 * - [ProgressListener.onProgress] returns `true` to continue, `false` to abort.
 * - [ProgressListener.onFinish] fires after the last token.
 */
class PhishingLlmAnalyzer {

    /** JNI callback interface — must match method signatures in llm_jni.cpp. */
    interface ProgressListener {
        fun onProgress(token: String): Boolean
        fun onFinish()
    }

    private val dispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()

    private var nativePtr: Long = 0L

    /**
     * Loads the MNN model. Blocking — must be called on IO dispatcher.
     * @throws RuntimeException if the native layer fails to load the model.
     */
    suspend fun load(modelFolder: File) {
        val configPath = File(modelFolder, "llm_config.json").absolutePath
        val ptr = withContext(dispatcher) { nativeCreate(configPath) }
        if (ptr == 0L) throw RuntimeException("Failed to load MNN model from $configPath")
        nativePtr = ptr
        Log.i(TAG, "Model loaded: $configPath")
    }

    fun llmProcessing(prompt: String): Flow<String> = callbackFlow {
        val thiz = this
        withContext(dispatcher) {
            check(nativePtr != 0L) { "PhishingLlmAnalyzer.load() must be called before llmProcessing()" }

            nativeGenerate(
                ptr = nativePtr,
                prompt = prompt,
                listener = object : ProgressListener {
                    override fun onProgress(token: String): Boolean {
                        thiz.trySendBlocking(token)
                        return true
                    }

                    override fun onFinish() {
                        close()
                    }
                },
            )
        }
    }

    /**
     * Runs inference with a raw pre-built [prompt] and streams the result token-by-token.
     * Blocking — call on IO dispatcher.
     */
    fun llmProcessing(
        prompt: String,
        onToken: (String) -> Unit,
        onDone: () -> Unit,
    ) {
        check(nativePtr != 0L) { "PhishingLlmAnalyzer.load() must be called before llmProcessing()" }
        nativeGenerate(
            ptr = nativePtr,
            prompt = prompt,
            listener = object : ProgressListener {
                override fun onProgress(token: String): Boolean {
                    onToken(token)
                    return true
                }

                override fun onFinish() = onDone()
            },
        )
    }

    /** Frees the native LLM instance. Safe to call multiple times. */
    fun release() {
        if (nativePtr != 0L) {
            nativeRelease(nativePtr)
            nativePtr = 0L
            Log.i(TAG, "Native LLM instance released")
        }
    }

    // ── JNI declarations ────────────────────────────────────────────────────

    private external fun nativeCreate(configPath: String): Long
    private external fun nativeGenerate(ptr: Long, prompt: String, listener: ProgressListener)
    private external fun nativeRelease(ptr: Long)

    companion object {
        private const val TAG = "PhishingLlmAnalyzer"

        init {
            System.loadLibrary("mnnllmphishing")
        }
    }
}