package com.safeNest.demo.features.scamAnalyzer.impl.utils.asr

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads whisper-tiny.tflite (~42 MB) from HuggingFace into the given directory.
 *
 * If the file is already present and non-empty the function returns immediately
 * without making any network requests.
 *
 * The download is written to `{name}.tmp` first, then atomically renamed to the
 * final path so a mid-download crash never leaves a corrupt file.
 */
object WhisperDownloader {

    private const val TAG = "WhisperDownloader"

    // Multilingual whisper-tiny (supports all 99 languages including Vietnamese).
    // Confirmed multilingual: 42.5 MB, Apache 2.0 — cik009/whisper on HuggingFace.
    private const val MODEL_URL =
        "https://huggingface.co/cik009/whisper/resolve/main/whisper-tiny.tflite"
    private const val MODEL_FILE_NAME = "whisper-tiny.tflite"

    private const val VOCAB_URL =
        "https://huggingface.co/openai/whisper-tiny/resolve/main/vocab.json"
    private const val VOCAB_FILE_NAME = "vocab.json"

    /**
     * Downloads [MODEL_FILE_NAME] and [VOCAB_FILE_NAME] into [modelDir] if not already present.
     * Returns the [File] pointing to the TFLite model.
     */
    suspend fun ensureModel(
        modelDir: File,
        onProgress: (percent: Int) -> Unit = {},
    ): File = withContext(Dispatchers.IO) {
        modelDir.mkdirs()

        data class Entry(val url: String, val name: String)

        val files = listOf(
            Entry(MODEL_URL, MODEL_FILE_NAME),
            Entry(VOCAB_URL, VOCAB_FILE_NAME),
        )

        val missing = files.filter { entry ->
            val f = File(modelDir, entry.name)
            !f.exists() || f.length() == 0L
        }

        if (missing.isEmpty()) {
            Log.i(TAG, "All Whisper files already present — skipping download")
            onProgress(100)
            return@withContext File(modelDir, MODEL_FILE_NAME)
        }

        // HEAD all missing files for total size (used for progress reporting)
        val totalBytes: Long = missing.sumOf { entry ->
            try {
                val conn = URL(entry.url).openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                conn.connect()
                val len = conn.contentLengthLong
                conn.disconnect()
                if (len > 0) len else 0L
            } catch (e: Exception) {
                Log.w(TAG, "HEAD failed for ${entry.name}: ${e.message}")
                0L
            }
        }

        var downloadedBytes = 0L

        for (entry in missing) {
            Log.i(TAG, "Downloading ${entry.name} …")
            val dest = File(modelDir, entry.name)
            val tmp  = File(modelDir, "${entry.name}.tmp")

            val conn = URL(entry.url).openConnection() as HttpURLConnection
            conn.connectTimeout = 30_000
            conn.readTimeout = 120_000
            conn.connect()

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                conn.disconnect()
                throw RuntimeException("HTTP $responseCode while downloading ${entry.name}")
            }

            tmp.outputStream().use { out ->
                conn.inputStream.use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val percent = (downloadedBytes * 100L / totalBytes).toInt().coerceIn(0, 99)
                            onProgress(percent)
                        }
                    }
                }
            }
            conn.disconnect()

            if (!tmp.renameTo(dest)) {
                tmp.delete()
                throw RuntimeException("Failed to rename ${entry.name}.tmp → ${entry.name}")
            }
            Log.i(TAG, "Downloaded ${entry.name} (${dest.length()} bytes)")
        }

        onProgress(100)
        File(modelDir, MODEL_FILE_NAME)
    }
}
