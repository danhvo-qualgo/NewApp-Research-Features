package com.safeNest.demo.permissionmanager.impl.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.permissionmanager.impl.domain.AppPermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionManagerViewModel @Inject constructor(
    private val appPermissionManager: AppPermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionManagerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { appPermissionManager.getInstalledAppsWithPermissions() }
                .onSuccess { apps ->
                    _uiState.update { it.copy(isLoading = false, allApps = apps) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.localizedMessage)
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleShowSystemApps() {
        _uiState.update { it.copy(showSystemApps = !it.showSystemApps) }
    }
}
