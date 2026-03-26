/*
 * LlamaEngine.kt — Kotlin wrapper for llama.cpp via JNI.
 *
 * Mirrors iOS LlamaEngine.swift. Loads GGUF model once, keeps it in memory,
 * generates tokens with streaming callback.
 */
package com.safenest.urlanalyzer.shared

import android.util.Log
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

interface ProgressListener {
    /** Called with each decoded token. Return false to stop generation. */
    fun onProgress(chunk: String): Boolean

    /** Called when generation is complete. */
    fun onFinish()
}

class LlamaEngine(
    private val modelPath: String,
    private val nCtx: Int = 4096,
    private val nGpuLayers: Int = 99,
    private val maxTokens: Int = 512
) {
    companion object {
        init {
            System.loadLibrary("safenest_jni")
        }

        private const val TAG = "LlamaEngine"
    }

    private val dispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()

    private var handle: Long = 0L

    @Volatile
    private var isLoaded: Boolean = false

    suspend fun ensureInit(): Boolean {
        return withContext(dispatcher) {
            if (isLoaded) return@withContext true
            isLoaded = true

            handle = nativeLoad(modelPath, nCtx, nGpuLayers)

            if (handle == 0L) {
                Log.e(TAG, "Failed to load model: $modelPath")
                return@withContext false
            } else {
                Log.i(TAG, "Model loaded: $modelPath")
            }
            true
        }
    }

    suspend fun generate(
        prompt: String,
        maxTokens: Int = this.maxTokens,
        listener: ProgressListener
    ) {
        withContext(dispatcher) {
            ensureInit()
            if (!isLoaded) {
                Log.e(TAG, "Model not loaded")
                listener.onFinish()
                return@withContext
            }
            nativeGenerate(handle, prompt, maxTokens, listener)
        }

    }

    suspend fun release() {
        withContext(dispatcher) {
            if (handle != 0L) {
                nativeRelease(handle)
                handle = 0L
            }
        }
    }

    protected suspend fun finalize() {
        withContext(dispatcher) {
            release()
        }

    }

    // JNI declarations — match llama_jni.cpp
    private external fun nativeLoad(modelPath: String, nCtx: Int, nGpuLayers: Int): Long
    private external fun nativeGenerate(
        handle: Long,
        prompt: String,
        maxTokens: Int,
        listener: ProgressListener
    )

    private external fun nativeRelease(handle: Long)
}
