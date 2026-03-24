package com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddBlacklistPatternUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlockListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetBlacklistPatternsUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.RemoveBlackListPatternUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    private val addBlacklistPatternUseCase: AddBlacklistPatternUseCase,
    private val getBlacklistPatternsUseCase: GetBlacklistPatternsUseCase,
    private val removeBlackListPatternUseCase: RemoveBlackListPatternUseCase,
    private val enableBlockListUseCase: EnableBlockListUseCase
) : ViewModel() {
    val defaultBlockPatterns: List<String> = listOf(
        "028********",
        "024********",
        "0203*******", "0204*******", "0205*******", "0206*******", "0207*******",
        "0208*******", "0209*******", "0210*******", "0211*******", "0212*******",
        "0213*******", "0214*******", "0215*******", "0216*******", "0218*******",
        "0219*******", "0220*******", "0221*******", "0222*******", "0225*******",
        "0226*******", "0227*******", "0228*******", "0229*******", "0232*******",
        "0233*******", "0234*******", "0235*******", "0236*******", "0237*******",
        "0238*******", "0239*******", "0251*******", "0252*******", "0254*******",
        "0255*******", "0256*******", "0257*******", "0258*******", "0259*******",
        "0260*******", "0261*******", "0262*******", "0263*******", "0269*******",
        "0270*******", "0271*******", "0272*******", "0273*******", "0274*******",
        "0275*******", "0276*******", "0277*******", "0290*******", "0291*******",
        "0292*******", "0293*******", "0294*******", "0296*******", "0297*******",
        "0299*******"
    )
    init {
        viewModelScope.launch(Dispatchers.IO) {
            defaultBlockPatterns.forEach {
                addBlacklistPatternUseCase(it, "Landline")
            }
            addBlacklistPatternUseCase("1800****", "Service number")
            addBlacklistPatternUseCase("1900****", "Service number")
        }
    }

    val blacklist = getBlacklistPatternsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isEnable = enableBlockListUseCase.isEnable()
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
            enableBlockListUseCase(isEnable)
        }
    }
}