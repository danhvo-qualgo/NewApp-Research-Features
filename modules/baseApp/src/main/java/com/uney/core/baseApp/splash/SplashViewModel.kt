package com.uney.core.baseApp.splash

import androidx.lifecycle.viewModelScope
import com.uney.core.baseApp.base.viewmodel.BaseViewModel
import com.uney.core.baseApp.base.viewmodel.DefaultBaseUiHost
import com.uney.core.coreutils.android.splashWorker.SplashWorkerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SplashViewModel @Inject constructor(
    private val splashScreenManager: SplashWorkerManager,
    defaultBaseUiHost: DefaultBaseUiHost,
) : BaseViewModel(defaultBaseUiHost) {
    private val _uiState = MutableStateFlow(UiState.Initial as UiState)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            showLoading(true)
            if (splashScreenManager.run()) {
                _uiState.value = UiState.Success
            } else {
                _uiState.value = UiState.Error
            }
            showLoading(false)
        }
    }
}

internal sealed interface UiState {
    data object Initial : UiState
    data object Success : UiState

    data object Error : UiState
}