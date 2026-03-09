package com.safeNest.features.call.callDetection.impl.presentation.ui.blacklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.features.call.callDetection.impl.domain.usecase.AddBlacklistPatternUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.AddNumberToWhiteListUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.GetBlacklistPatternsUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.GetWhiteListUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.RemoveBlackListPatternUseCase
import com.safeNest.features.call.callDetection.impl.domain.usecase.RemoveWhiteListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistModel @Inject constructor(
    private val addBlacklistPatternUseCase: AddBlacklistPatternUseCase,
    private val getBlacklistPatternsUseCase: GetBlacklistPatternsUseCase,
    private val removeBlackListPatternUseCase: RemoveBlackListPatternUseCase
) : ViewModel() {

    val blacklist = getBlacklistPatternsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
}