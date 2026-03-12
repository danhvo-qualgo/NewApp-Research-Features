package com.safeNest.demo.features.phishingDetection.impl.presentation

import android.util.Log
import com.safeNest.demo.features.phishingDetection.impl.presentation.models.WebsiteMetadata
import java.io.File

/**
 * Kotlin facade over the native MNN LLM via [libmnnllmphishing.so].
 *
 * Lifecycle:
 * 1. Call [load] on [kotlinx.coroutines.Dispatchers.IO] to initialise the model.
 * 2. Call [analyze] (also on IO) — streaming tokens arrive via [onToken]; [onDone] fires last.
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

    private var nativePtr: Long = 0L

    /**
     * Loads the MNN model. Blocking — must be called on IO dispatcher.
     * @throws RuntimeException if the native layer fails to load the model.
     */
    fun load(modelFolder: File) {
        val configPath = File(modelFolder, "llm_config.json").absolutePath
        val ptr = nativeCreate(configPath)
        if (ptr == 0L) throw RuntimeException("Failed to load MNN model from $configPath")
        nativePtr = ptr
        Log.i(TAG, "Model loaded: $configPath")
    }

    /**
     * Runs inference and streams the result token-by-token.
     * Blocking — call on IO dispatcher.
     */
    fun analyze(
        url: String,
        metadata: WebsiteMetadata,
        onToken: (String) -> Unit,
        onDone: () -> Unit,
    ) {
        check(nativePtr != 0L) { "PhishingLlmAnalyzer.load() must be called before analyze()" }
        nativeGenerate(
            ptr = nativePtr,
            prompt = buildPrompt(url, metadata),
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

    // ── Prompt builder ───────────────────────────────────────────────────────

    private fun buildPrompt(url: String, metadata: WebsiteMetadata): String {
        val body = metadata.bodyText.take(400)
        val description = metadata.ogDescription.ifBlank { metadata.description }.take(200)
        // Qwen3.5-2B uses ChatML format: <|im_start|>role\ncontent<|im_end|>
        return buildString {
            append("<|im_start|>system\n")
            append("You are a cybersecurity expert specializing in phishing detection. ")
            append("Analyze the website information and its body text and give a concise risk assessment.")
            append("<|im_end|>\n")
            append("<|im_start|>user\n")
            append("Analyze this website for phishing risk:\n\n")
            append("URL: $url\n")
            if (metadata.title.isNotBlank()) append("Title: ${metadata.title}\n")
            if (description.isNotBlank()) append("Description: $description\n")
            if (body.isNotBlank()) append("Body text (take 400 first): $body\n")
            append("\nRespond text only with: Risk level (Likely Scam | Suspicious | Likely Legit | Unknown), Confidence (0% to 100%), and 2-3 key signals.")
            append("<|im_end|>\n")
            append("<|im_start|>assistant\n")
        }
    }

    companion object {
        private const val TAG = "PhishingLlmAnalyzer"

        init {
            System.loadLibrary("mnnllmphishing")
        }
    }
}
