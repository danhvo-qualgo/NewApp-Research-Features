package com.safenest.urlanalyzer

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class ModelDownloader @Inject constructor(
    @param:ApplicationContext private val ctx: Context,
    private val config: ModelConfig,
) {
    private val modelFile: File
        get() = File(ctx.filesDir, config.modelName)

    fun isDownloaded(): Boolean {
        val file = modelFile
        return file.exists() && file.length() > 0L
    }

    /**
     * Returns the model [File], downloading it first if needed.
     * [onProgress] is called with values 0–100 on the calling dispatcher.
     */
    suspend fun ensureDownloaded(onProgress: ((Int) -> Unit)? = null): File {
        return withContext(Dispatchers.IO) {
            val file = modelFile

            if (isDownloaded()) {
                onProgress?.invoke(100)
                return@withContext file
            }

            Log.d(TAG, "Downloading model from ${config.downloadUrl}")

            try {
                downloadFromUrl(config.downloadUrl, file, onProgress)
            } catch (e: Exception) {
                if (file.exists()) file.delete()
                Log.e(TAG, "Model download failed", e)
                throw e
            }

            Log.d(TAG, "Download finished, bytes=${file.length()}")
            file
        }
    }

    /**
     * Deletes the local model file. Does not throw if the file is already absent.
     */
    suspend fun deleteFile() {
        withContext(Dispatchers.IO) {
            val file = modelFile
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Model file deleted")
            }
        }
    }

    // ── private ──────────────────────────────────────────────────────────────

    private fun downloadFromUrl(
        urlString: String,
        dest: File,
        onProgress: ((Int) -> Unit)?,
    ) {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            connectTimeout = 60_000
            readTimeout = 600_000
            instanceFollowRedirects = true
        }

        try {
            connection.connect()

            val code = connection.responseCode
            if (code !in 200..299) {
                throw RuntimeException("Download failed: HTTP $code")
            }

            val contentLength = connection.contentLengthLong
            if (contentLength <= 0) onProgress?.invoke(0)

            connection.inputStream.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyToWithProgress(output, contentLength, onProgress)
                }
            }

            onProgress?.invoke(100)
        } finally {
            connection.disconnect()
        }
    }

    private fun InputStream.copyToWithProgress(
        output: OutputStream,
        totalSize: Long,
        onProgress: ((Int) -> Unit)?,
    ) {
        if (onProgress == null) {
            copyTo(output)
            return
        }

        val buffer = ByteArray(8192)
        var bytes = read(buffer)
        var copied = 0L
        var lastReported = -1

        while (bytes >= 0) {
            output.write(buffer, 0, bytes)
            copied += bytes

            if (totalSize > 0) {
                val progress = (copied * 100 / totalSize).toInt().coerceIn(0, 100)
                if (progress != lastReported) {
                    lastReported = progress
                    onProgress(progress)
                }
            }

            bytes = read(buffer)
        }
    }

    private companion object {
        const val TAG = "ModelDownloader"
    }
}