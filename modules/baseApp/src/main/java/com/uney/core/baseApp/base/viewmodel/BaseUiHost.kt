package com.uney.core.baseApp.base.viewmodel

import com.uney.core.baseApp.base.model.ApiError
import com.uney.core.baseApp.base.model.UiText
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BaseUiHost {
    val baseUiState: StateFlow<BaseUiState>

    val baseUiEvent: SharedFlow<BaseUiEvent>

    fun showLoading(loading: Boolean)

    fun showErrorDialog(
        title: UiText = UiText.Empty,
        message: UiText = UiText.Empty,
    )

    fun handleCommonApiError(apiError: ApiError) {
    }
}

sealed interface BaseUiEvent {
    data class ErrorDialog(val title: UiText, val message: UiText) : BaseUiEvent
}

data class BaseUiState(
    val loading: Boolean = false
)