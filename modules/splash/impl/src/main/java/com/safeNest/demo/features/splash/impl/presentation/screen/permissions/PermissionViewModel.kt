package com.safeNest.demo.features.splash.impl.presentation.screen.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.splash.impl.domain.PermissionManager
import com.safeNest.demo.features.splash.impl.domain.model.PermissionRequestType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PermissionViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()


    private val _event = Channel<PermissionEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadPermissionStates()
    }

    fun onAction(action: PermissionAction) {
        when (action) {
            is PermissionAction.TogglePermission -> handleToggle(action.type)
            is PermissionAction.UpdatePermissionGrantedState -> handleUpdatePermissionState(action.type, action.isGranted)
        }
    }

    /** Re-reads every permission from [PermissionManager] and updates the state. */
    fun refreshPermissionStates() {
        loadPermissionStates()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadPermissionStates() {
        val states = PermissionType.entries.associateWith { type ->
            permissionManager.isGranted(type)
        }
        _uiState.update { it.copy(permissionStates = states) }
    }

    private fun handleToggle(type: PermissionType) {
        // Immediately reflect whatever the system now reports.
        when(type.requestType) {
            is PermissionRequestType.RunTime,
            is PermissionRequestType.RunTimes -> {
                viewModelScope.launch {
                    _event.send(PermissionEvent.RequestPermissionEvent(type))
                }

            }
            is PermissionRequestType.Settings -> {
                permissionManager.requestPermission(type)
            }
        }
        loadPermissionStates()
    }

    private fun handleUpdatePermissionState(type: PermissionType, isGranted: Boolean) {
        _uiState.update { it.copy(permissionStates = it.permissionStates + (type to isGranted)) }
    }
}
