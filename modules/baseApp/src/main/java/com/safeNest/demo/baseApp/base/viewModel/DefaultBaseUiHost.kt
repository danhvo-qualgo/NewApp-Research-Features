package com.safeNest.demo.baseApp.base.viewModel

import com.safeNest.demo.baseApp.base.model.UiText
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@ViewModelScoped
class DefaultBaseUiHost @Inject constructor() :
    com.safeNest.demo.baseApp.base.viewModel.BaseUiHost {

    override val baseUiState = MutableStateFlow(_root_ide_package_.com.safeNest.demo.baseApp.base.viewModel.BaseUiState())

    override val baseUiEvent = MutableSharedFlow<com.safeNest.demo.baseApp.base.viewModel.BaseUiEvent>()

    override fun showLoading(loading: Boolean) {
        baseUiState.update { it.copy(loading = loading) }
    }

    override fun showErrorDialog(title: com.safeNest.demo.baseApp.base.model.UiText, message: com.safeNest.demo.baseApp.base.model.UiText) {}
}