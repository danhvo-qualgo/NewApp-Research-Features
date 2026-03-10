package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OptionViewModel @Inject constructor(
    private val modelManager: ModelManager,
) : ViewModel() {

    val modelState: StateFlow<ModelState> = modelManager.state

    init {
        viewModelScope.launch {
            modelManager.ensureReady()
        }
    }
}
