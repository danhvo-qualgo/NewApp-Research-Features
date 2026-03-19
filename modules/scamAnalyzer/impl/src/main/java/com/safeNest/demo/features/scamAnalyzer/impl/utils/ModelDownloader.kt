package com.safeNest.demo.features.phishingDetection.impl.presentation

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads the MobileLLM-1B-MNN model files from HuggingFace into
 * `filesDir/.mnnmodels/MobileLLM-1B/`.
 *
 * If all 7 files are already present and non-empty the function returns
 * immediately without making any network requests.
 *
 * Downloads are written to `{name}.tmp` first, then atomically renamed to
 * the final path so that a mid-download crash never leaves a corrupt file
 * that is mistaken for a complete one on the next launch.
 */
object ModelDownloader {

    private const val TAG = "ModelDownloader"

    private const val BASE_URL =
        "https://huggingface.co/taobao-mnn/Qwen3-0.6B-MNN/resolve/main"
// try qwen3:06b-q4_k_m
    private val MODEL_FILES = listOf(
        "config.json",
        "llm_config.json",
        "tokenizer.txt",
        "llm.mnn",
//        "llm.mnn.json",
        "llm.mnn.weight",
    )

    suspend fun ensureModel(
        modelDir: File,
        onProgress: (percent: Int) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        modelDir.mkdirs()

        // Determine which files still need to be fetched
        val missing = MODEL_FILES.filter { name ->
            val f = File(modelDir, name)
            !f.exists() || f.length() == 0L
        }

        if (missing.isEmpty()) {
            Log.i(TAG, "All model files already present — skipping download")
            onProgress(100)
            return@withContext modelDir
        }

        Log.i(TAG, "Need to download ${missing.size} file(s): $missing")

        // ── Phase 1: HEAD each missing file to get Content-Length ────────────
        val fileSizes: Map<String, Long> = missing.associateWith { name ->
            try {
                val conn = URL("$BASE_URL/$name").openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                conn.connect()
                val len = conn.contentLengthLong
                conn.disconnect()
                if (len > 0) len else 0L
            } catch (e: Exception) {
                Log.w(TAG, "HEAD failed for $name: ${e.message}")
                0L
            }
        }

        val totalBytes = fileSizes.values.sum().takeIf { it > 0 }
        var downloadedBytes = 0L

        // ── Phase 2: Download each missing file sequentially ─────────────────
        for (name in missing) {
            val dest = File(modelDir, name)
            val tmp = File(modelDir, "$name.tmp")

            Log.i(TAG, "Downloading $name …")

            val conn = URL("$BASE_URL/$name").openConnection() as HttpURLConnection
            conn.connectTimeout = 30_000
            conn.readTimeout = 60_000
            conn.connect()

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                conn.disconnect()
                throw RuntimeException("HTTP $responseCode while downloading $name")
            }

            tmp.outputStream().use { out ->
                conn.inputStream.use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes != null && totalBytes > 0) {
                            val percent = (downloadedBytes * 100L / totalBytes).toInt()
                                .coerceIn(0, 99) // reserve 100 for full completion
                            onProgress(percent)
                        }
                    }
                }
            }

            conn.disconnect()

            // Atomic rename: only replace the final file once fully written
            if (!tmp.renameTo(dest)) {
                tmp.delete()
                throw RuntimeException("Failed to rename $name.tmp → $name")
            }
            Log.i(TAG, "Downloaded $name (${dest.length()} bytes)")
        }

        onProgress(100)
        Log.i(TAG, "Model download complete: ${modelDir.absolutePath}")
        modelDir
    }
}
