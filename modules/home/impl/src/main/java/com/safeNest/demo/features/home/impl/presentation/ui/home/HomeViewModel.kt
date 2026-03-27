package com.safeNest.demo.features.home.impl.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.home.impl.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

data class HomeUiState(
    val dnsPermissionState: Boolean = false,
    val showDownloadDialog: Boolean = false,
    val isLoading: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _downloadEvent = Channel<InputStream>()
    val downloadEvent = _downloadEvent.receiveAsFlow()

    init {
        observeDnsPermission()
    }

    private fun observeDnsPermission() {
        viewModelScope.launch {
            homeRepository.getDnsPermissionStateFlow().collect { isGranted ->
                _uiState.update { it.copy(dnsPermissionState = isGranted) }
            }
        }
    }

    fun toggleDnsPermission() {
        _uiState.update { it.copy(showDownloadDialog = true) }
    }

    fun closeDownloadDialog() {
        _uiState.update { it.copy(showDownloadDialog = false) }
    }

    fun downloadCaCert() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDownloadDialog = false) }
            homeRepository.downloadCaCert()
                .onSuccess {
                    _downloadEvent.send(it)
                }
                .onFailure { error  ->
                    Log.d("xxx", "donw cert fail : ${error.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun onSavedComplete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false) }
            homeRepository.setDnsPermissionState(true)
        }
    }
}