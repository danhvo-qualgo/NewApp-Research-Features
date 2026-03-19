package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr.WhisperModelManager
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.asr.WhisperModelState
import javax.inject.Inject

@HiltViewModel
class OptionViewModel @Inject constructor(
    private val modelManager: ModelManager,
    private val whisperModelManager: WhisperModelManager,
) : ViewModel() {

    val modelState: StateFlow<ModelState> = modelManager.state
    val whisperState: StateFlow<WhisperModelState> = whisperModelManager.state

    init {
        viewModelScope.launch { modelManager.ensureReady() }
        viewModelScope.launch { whisperModelManager.ensureReady() }
    }
}
