package com.safeNest.features.call.callDetection.impl.presentation.ui.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.features.call.callDetection.impl.domain.usecase.AddNumberToWhiteListUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.EnableWhiteListUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.GetWhiteListUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.RemoveWhiteListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhitelistModel @Inject constructor(
    private val addNumberToWhiteListUseCase: AddNumberToWhiteListUseCase,
    private val getWhitelistUseCase: GetWhiteListUseCase,
    private val removeWhiteListUseCase: RemoveWhiteListUseCase,
    private val enableWhiteListUseCase: EnableWhiteListUseCase
) : ViewModel() {

    val whitelist = getWhitelistUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val isEnable = enableWhiteListUseCase.isEnable()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun add(number: String) {
        viewModelScope.launch {
            addNumberToWhiteListUseCase(number)
        }
    }

    fun remove(number: String) {
        viewModelScope.launch {
            removeWhiteListUseCase(number)
        }
    }

    fun enable(isEnable: Boolean) {
        viewModelScope.launch {
            enableWhiteListUseCase(isEnable)
        }
    }
}