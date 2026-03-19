package com.safeNest.demo.features.home.impl.presentation.ui.mediaPreview

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject

data class MediaPreviewUiState(
    val fileName: String = "Media File",
    val fileSize: String = "0 MB",
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface MediaPreviewEvent {
    data object AnalysisSuccess : MediaPreviewEvent
}

@HiltViewModel
class MediaPreviewViewModel @Inject constructor(
    private val application: Application,
    private val analyzeUseCase: AnalyzeUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MediaPreviewUiState())
    val uiState: StateFlow<MediaPreviewUiState> = _uiState.asStateFlow()

    private val _events = Channel<MediaPreviewEvent>()
    val events = _events.receiveAsFlow()

    fun loadMediaInfo(uri: Uri) {
        viewModelScope.launch {
            try {
                Log.d("MediaPreviewViewModel", "Loading media info for URI: $uri")
                val (fileName, fileSize) = getFileInfo(uri)
                Log.d("MediaPreviewViewModel", "File info loaded: name=$fileName, size=$fileSize")
                _uiState.value = _uiState.value.copy(
                    fileName = fileName,
                    fileSize = fileSize
                )
            } catch (e: Exception) {
                Log.e("MediaPreviewViewModel", "Error loading file info", e)
                _uiState.value = _uiState.value.copy(
                    fileName = "Audio File",
                    fileSize = "Unknown"
                )
            }
        }
    }

    fun analyzeAudio(audioUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAnalyzing = true, errorMessage = null)

                val result = analyzeUseCase(AnalysisInput.Audio(audioUri))
                Log.d("MediaPreviewViewModel", "Analysis result: $result")

                _uiState.value = _uiState.value.copy(isAnalyzing = false)
                _events.send(MediaPreviewEvent.AnalysisSuccess)
            } catch (e: Exception) {
                Log.e("MediaPreviewViewModel", "Error analyzing audio", e)
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = e.message ?: "Failed to analyze audio"
                )
            }
        }
    }

    fun analyzeImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAnalyzing = true, errorMessage = null)

                val result = analyzeUseCase(AnalysisInput.Image(imageUri))
                Log.d("MediaPreviewViewModel", "Analysis result: $result")

                _uiState.value = _uiState.value.copy(isAnalyzing = false)
                _events.send(MediaPreviewEvent.AnalysisSuccess)
            } catch (e: Exception) {
                Log.e("MediaPreviewViewModel", "Error analyzing image", e)
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = e.message ?: "Failed to analyze image"
                )
            }
        }
    }

    private fun getFileInfo(uri: Uri): Pair<String, String> {
        var fileName = "Media File"
        var fileSize = "Unknown"

        try {
            Log.d("MediaPreviewViewModel", "Getting file info for URI: $uri (scheme=${uri.scheme})")
            
            // Handle file:// URIs (from recording)
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                Log.d("MediaPreviewViewModel", "File URI - path=${file.path}, exists=${file.exists()}")
                if (file.exists()) {
                    fileName = file.name
                    fileSize = formatFileSize(file.length())
                    Log.d("MediaPreviewViewModel", "File info from file:// - name=$fileName, size=$fileSize")
                }
                return Pair(fileName, fileSize)
            }

            // Handle content:// URIs (from file picker)
            if (uri.scheme == "content") {
                try {
                    application.contentResolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            // Get file name
                            try {
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (nameIndex >= 0) {
                                    val name = cursor.getString(nameIndex)
                                    if (!name.isNullOrEmpty()) {
                                        fileName = name
                                        Log.d("MediaPreviewViewModel", "Got file name: $fileName")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MediaPreviewViewModel", "Error getting file name", e)
                            }
                            
                            // Get file size
                            try {
                                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                                    val bytes = cursor.getLong(sizeIndex)
                                    fileSize = formatFileSize(bytes)
                                    Log.d("MediaPreviewViewModel", "Got file size: $fileSize")
                                }
                            } catch (e: Exception) {
                                Log.e("MediaPreviewViewModel", "Error getting file size", e)
                            }
                            
                            Log.d("MediaPreviewViewModel", "Final info from content:// - name=$fileName, size=$fileSize")
                        } else {
                            Log.w("MediaPreviewViewModel", "Cursor is empty - moveToFirst() returned false")
                        }
                    } ?: run {
                        Log.w("MediaPreviewViewModel", "ContentResolver.query() returned null cursor")
                    }
                } catch (e: Exception) {
                    Log.e("MediaPreviewViewModel", "Error querying ContentResolver", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MediaPreviewViewModel", "Error reading file info: ${e.message}", e)
        }

        Log.d("MediaPreviewViewModel", "Returning: fileName=$fileName, fileSize=$fileSize")
        return Pair(fileName, fileSize)
    }

    private fun formatFileSize(bytes: Long): String {
        val df = DecimalFormat("#.#")
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${df.format(bytes / 1024.0)} KB"
            bytes < 1024 * 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
            else -> "${df.format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
}
