package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.Phonenumber
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddBlacklistPatternUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddToMasterBlockListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddToMasterWhiteListUseCase
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
class MissingCallViewModel @Inject constructor(
    private val addToMasterBlockListUseCase: AddToMasterBlockListUseCase,
    private val addToMasterWhiteListUseCase: AddToMasterWhiteListUseCase,
) : ViewModel() {
    fun addToBlocklist(phoneNumber: String) {
        viewModelScope.launch {
            addToMasterBlockListUseCase(phoneNumber)
        }
    }
    fun addToWhitelist(phoneNumber: String, name: String) {
        viewModelScope.launch {
            addToMasterWhiteListUseCase(phoneNumber, name)
        }
    }
}