package com.uney.core.baseApp.base.viewModel

import androidx.lifecycle.ViewModel
import com.uney.core.baseApp.base.model.ApiError
import com.uney.core.baseApp.base.model.UiText
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel(private val delegate: BaseUiHost) : ViewModel() {

    val baseUiState: StateFlow<BaseUiState> = delegate.baseUiState

    val baseUiEvent: SharedFlow<BaseUiEvent> = delegate.baseUiEvent

    fun showLoading(loading: Boolean) {
        delegate.showLoading(loading)
    }

    fun showErrorDialog(
        title: UiText = UiText.Empty,
        message: UiText = UiText.Empty,
    ) {
        delegate.showErrorDialog(title, message)
    }

    fun handleCommonApiError(apiError: ApiError) {
        delegate.handleCommonApiError(apiError)
    }
}

