package com.uney.core.baseApp.base.viewmodel

import com.uney.core.baseApp.base.model.UiText
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@ViewModelScoped
class DefaultBaseUiHost @Inject constructor() : BaseUiHost {
    override val baseUiState = MutableStateFlow(BaseUiState())

    override val baseUiEvent = MutableSharedFlow<BaseUiEvent>()

    override fun showLoading(loading: Boolean) {
        baseUiState.update { it.copy(loading = loading) }
    }

    override fun showErrorDialog(
        title: UiText,
        message: UiText
    ) {}
}