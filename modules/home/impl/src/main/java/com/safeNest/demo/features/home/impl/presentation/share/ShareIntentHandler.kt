package com.safeNest.demo.features.home.impl.presentation.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed class ShareData {
    data class Text(val text: String) : ShareData()
    data class Audio(val uri: Uri) : ShareData()
    data class Image(val uri: Uri) : ShareData()
}

class ShareIntentHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ShareIntentHandler"
    }

    fun extractShareData(intent: Intent?): ShareData? {
        if (intent?.action != Intent.ACTION_SEND) {
            return null
        }
        Log.d("###", "Receive shared intent.")

        return when (intent.type) {
            "text/plain" -> extractTextData(intent)
            in listOf("audio/*", "audio/mpeg", "audio/mp3", "audio/wav") -> extractAudioData(intent)
            else -> {
                if (intent.type?.startsWith("image/") == true) {
                    extractImageData(intent)
                } else null
            }
        }
    }

    private fun extractTextData(intent: Intent): ShareData? {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        return if (!sharedText.isNullOrBlank()) {
            Log.d(TAG, "Extracted shared text: ${sharedText.take(50)}...")
            ShareData.Text(sharedText)
        } else null
    }

    private fun extractAudioData(intent: Intent): ShareData? {
        val audioUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        return audioUri?.let {
            Log.d(TAG, "Extracted shared audio URI: $audioUri")
            ShareData.Audio(it)
        }
    }

    private fun extractImageData(intent: Intent): ShareData? {
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        return imageUri?.let {
            Log.d(TAG, "Extracted shared image URI: $imageUri")
            ShareData.Image(it)
        }
    }

    suspend fun copyToAppStorage(uri: Uri, type: ShareType): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, "shared_media")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val fileName = getFileName(uri, type)
                val destinationFile = File(cacheDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d(
                    TAG,
                    "File copied to: ${destinationFile.absolutePath}, size: ${destinationFile.length()} bytes"
                )
                Uri.fromFile(destinationFile)
            } catch (e: Exception) {
                Log.e(TAG, "Error copying file to app storage", e)
                null
            }
        }
    }

    private fun getFileName(uri: Uri, type: ShareType): String {
        var fileName = "shared_${System.currentTimeMillis()}"

        try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrEmpty()) {
                            return name
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get original filename: $e")
        }

        val extension = getFileExtension(uri, type)
        return "${type.name.lowercase()}_${System.currentTimeMillis()}$extension"
    }

    private fun getFileExtension(uri: Uri, type: ShareType): String {
        val mimeType = context.contentResolver.getType(uri)
        return when (type) {
            ShareType.AUDIO -> {
                when {
                    mimeType?.contains("mp3") == true -> ".mp3"
                    mimeType?.contains("wav") == true -> ".wav"
                    mimeType?.contains("m4a") == true -> ".m4a"
                    else -> ".mp3"
                }
            }
            ShareType.IMAGE -> {
                when {
                    mimeType?.contains("png") == true -> ".png"
                    mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> ".jpg"
                    mimeType?.contains("webp") == true -> ".webp"
                    else -> ".jpg"
                }
            }
        }
    }
}

enum class ShareType {
    AUDIO,
    IMAGE
}
