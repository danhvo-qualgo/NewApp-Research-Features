package com.safeNest.demo.baseApp.base.viewModel

import androidx.lifecycle.ViewModel
import com.safeNest.demo.baseApp.base.model.ApiError
import com.safeNest.demo.baseApp.base.model.UiText
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel(private val delegate: com.safeNest.demo.baseApp.base.viewModel.BaseUiHost) : ViewModel() {

    val baseUiState: StateFlow<com.safeNest.demo.baseApp.base.viewModel.BaseUiState> = delegate.baseUiState

    val baseUiEvent: SharedFlow<com.safeNest.demo.baseApp.base.viewModel.BaseUiEvent> = delegate.baseUiEvent

    fun showLoading(loading: Boolean) {
        delegate.showLoading(loading)
    }

    fun showErrorDialog(
        title: com.safeNest.demo.baseApp.base.model.UiText = _root_ide_package_.com.safeNest.demo.baseApp.base.model.UiText.Empty,
        message: com.safeNest.demo.baseApp.base.model.UiText = _root_ide_package_.com.safeNest.demo.baseApp.base.model.UiText.Empty,
    ) {
        delegate.showErrorDialog(title, message)
    }

    fun handleCommonApiError(apiError: com.safeNest.demo.baseApp.base.model.ApiError) {
        delegate.handleCommonApiError(apiError)
    }
}

