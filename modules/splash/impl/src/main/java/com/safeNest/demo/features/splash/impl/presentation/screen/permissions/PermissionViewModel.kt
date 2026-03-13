package net.qualgo.safeNest.onboarding.impl.permission.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.qualgo.safeNest.onboarding.api.permission.PermissionManager
import net.qualgo.safeNest.onboarding.api.permission.PermissionType
import javax.inject.Inject

@HiltViewModel
internal class PermissionViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        loadPermissionStates()
    }

    fun onAction(action: PermissionAction) {
        when (action) {
            is PermissionAction.TogglePermission -> handleToggle(action.type)
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
            permissionManager.checkPermission(type)
        }
        _uiState.update { it.copy(permissionStates = states) }
    }

    private fun handleToggle(type: PermissionType) {
        if (type.isSubscriptionRequired) return
        permissionManager.requestPermission(type)
        // Immediately reflect whatever the system now reports.
        loadPermissionStates()
    }
}
