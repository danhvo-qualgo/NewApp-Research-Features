package com.safeNest.features.core.authChallenge.impl.presentation.screen

import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge

internal data class VerifySsoUiState(
    val isLoading: Boolean = false,
    val dataToVerify: AuthChallenge = AuthChallenge.EMPTY
)

internal sealed interface VerifySsoUiEvent {
    data class Error(val message: String) : VerifySsoUiEvent
    data class NavigateAuthChallenge(val data: AuthChallenge) : VerifySsoUiEvent
}
