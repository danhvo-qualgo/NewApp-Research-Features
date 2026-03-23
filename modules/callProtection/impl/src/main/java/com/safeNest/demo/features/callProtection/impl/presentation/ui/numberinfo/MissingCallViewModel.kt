package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddToMasterBlockListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddToMasterWhiteListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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