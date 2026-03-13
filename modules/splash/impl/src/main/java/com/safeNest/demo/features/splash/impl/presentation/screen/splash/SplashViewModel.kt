package com.safeNest.demo.features.splash.impl.presentation.screen.splash

import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.baseApp.base.viewModel.BaseViewModel
import com.safeNest.demo.features.baseApp.base.viewModel.DefaultBaseUiHost
import com.safeNest.demo.features.splash.impl.domain.PermissionManager
import com.uney.core.utils.android.worker.AppWorkerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SplashViewModel @Inject constructor(
    private val workerManager: AppWorkerManager,
    private val permissionManager: PermissionManager,
    defaultBaseUiHost: DefaultBaseUiHost,
) : BaseViewModel(defaultBaseUiHost) {
    private val _uiState = MutableStateFlow(UiState.Initial as UiState)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            showLoading(true)
            val workerResult = workerManager.run()
            val isAllPermissionGranted = permissionManager.isAllPermissionGrantedFlow().first()
            if (workerResult) {
                _uiState.value = UiState.Success(allPermissionGranted = isAllPermissionGranted)
            } else {
                _uiState.value = UiState.Error
            }
            showLoading(false)
        }

    }
}

internal sealed interface UiState {
    data object Initial : UiState
    data class Success(val allPermissionGranted: Boolean = false) : UiState

    data object Error : UiState
}