package com.safeNest.demo.features.baseApp.base.viewModel

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BaseUiHost {

    val baseUiState: StateFlow<com.safeNest.demo.features.baseApp.base.viewModel.BaseUiState>

    val baseUiEvent: SharedFlow<com.safeNest.demo.features.baseApp.base.viewModel.BaseUiEvent>

    fun showLoading(loading: Boolean)

    fun showErrorDialog(
        title: com.safeNest.demo.features.baseApp.base.model.UiText = _root_ide_package_.com.safeNest.demo.features.baseApp.base.model.UiText.Empty,
        message: com.safeNest.demo.features.baseApp.base.model.UiText = _root_ide_package_.com.safeNest.demo.features.baseApp.base.model.UiText.Empty
    )

    fun handleCommonApiError(apiError: com.safeNest.demo.features.baseApp.base.model.ApiError) {}
}

sealed interface BaseUiEvent {
    data class ErrorDialog(
        val title: com.safeNest.demo.features.baseApp.base.model.UiText,
        val message: com.safeNest.demo.features.baseApp.base.model.UiText
    ) : BaseUiEvent
}

data class BaseUiState(
    val loading: Boolean = false
)