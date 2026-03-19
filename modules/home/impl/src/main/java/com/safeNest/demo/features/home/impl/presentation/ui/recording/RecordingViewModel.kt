package com.safeNest.demo.features.home.impl.presentation.ui.recording

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class RecordingUiState(
    val isRecording: Boolean = false,
    val recordingFilePath: String? = null,
    val recordingUri: Uri? = null,
    val errorMessage: String? = null
)

sealed interface RecordingEvent {
    data class RecordingStopped(val audioUri: Uri?) : RecordingEvent
}

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private val _events = Channel<RecordingEvent>()
    val events = _events.receiveAsFlow()

    fun startRecording() {
        viewModelScope.launch {
            try {
                AudioRecordingService.start(application)
                _uiState.value = _uiState.value.copy(isRecording = true)
            } catch (e: Exception) {
                Log.e("RecordingViewModel", "Error starting recording", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to start recording"
                )
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                AudioRecordingService.stop(application)
                _uiState.value = _uiState.value.copy(isRecording = false)
                
                // Get the latest recorded file
                val recordedFile = getLatestRecordedFile()
                if (recordedFile != null) {
                    val recordingUri = Uri.fromFile(recordedFile)
                    _uiState.value = _uiState.value.copy(
                        recordingFilePath = recordedFile.absolutePath,
                        recordingUri = recordingUri
                    )
                    
                    // Emit event with audio URI
                    _events.send(RecordingEvent.RecordingStopped(recordingUri))
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Recording file not found"
                    )
                    _events.send(RecordingEvent.RecordingStopped(null))
                }
            } catch (e: Exception) {
                Log.e("RecordingViewModel", "Error stopping recording", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to stop recording"
                )
            }
        }
    }

    private fun getLatestRecordedFile(): File? {
        val dir = File(application.getExternalFilesDir(null), "audioRecording")
        return dir.listFiles()
            ?.filter { it.isFile && it.extension == "mp3" }
            ?.maxByOrNull { it.lastModified() }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure recording is stopped when ViewModel is cleared
        if (_uiState.value.isRecording) {
            AudioRecordingService.stop(application)
        }
    }
}

