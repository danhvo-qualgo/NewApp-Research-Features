package com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddBlacklistPatternUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlackListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetBlacklistPatternsUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.RemoveBlackListPatternUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    private val addBlacklistPatternUseCase: AddBlacklistPatternUseCase,
    private val getBlacklistPatternsUseCase: GetBlacklistPatternsUseCase,
    private val removeBlackListPatternUseCase: RemoveBlackListPatternUseCase,
    private val enableBlackListUseCase: EnableBlackListUseCase
) : ViewModel() {

    val blacklist = getBlacklistPatternsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isEnable = enableBlackListUseCase.isEnable()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun add(pattern: String, description: String) {
        viewModelScope.launch {
            addBlacklistPatternUseCase(pattern = pattern, description = description)
        }
    }

    fun remove(pattern: String) {
        viewModelScope.launch {
            removeBlackListPatternUseCase(pattern)
        }
    }

    fun enable(isEnable: Boolean) {
        viewModelScope.launch {
            enableBlackListUseCase(isEnable)
        }
    }
}