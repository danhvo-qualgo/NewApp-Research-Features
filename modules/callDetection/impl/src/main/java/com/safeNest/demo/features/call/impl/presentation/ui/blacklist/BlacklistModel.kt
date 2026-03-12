package com.safeNest.demo.features.call.impl.presentation.ui.blacklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.call.impl.domain.usecase.AddBlacklistPatternUseCase
import com.safeNest.demo.features.call.impl.domain.usecase.EnableBlackListUseCase
import com.safeNest.demo.features.call.impl.domain.usecase.GetBlacklistPatternsUseCase
import com.safeNest.demo.features.call.impl.domain.usecase.RemoveBlackListPatternUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistModel @Inject constructor(
    private val addBlacklistPatternUseCase: AddBlacklistPatternUseCase,
    private val getBlacklistPatternsUseCase: GetBlacklistPatternsUseCase,
    private val removeBlackListPatternUseCase: RemoveBlackListPatternUseCase,
    private val enableBlackListUseCase: EnableBlackListUseCase
) : ViewModel() {

    val blacklist = getBlacklistPatternsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isEnable = enableBlackListUseCase.isEnable()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun add(number: String) {
        viewModelScope.launch {
            addBlacklistPatternUseCase(number)
        }
    }

    fun remove(number: String) {
        viewModelScope.launch {
            removeBlackListPatternUseCase(number)
        }
    }

    fun enable(isEnable: Boolean) {
        viewModelScope.launch {
            enableBlackListUseCase(isEnable)
        }
    }
}