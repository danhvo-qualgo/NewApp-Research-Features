package net.qualgo.safeNest.core.authChallenge.impl.presentation.screen

import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge

internal data class VerifySsoUiState(
    val isLoading: Boolean = false,
    val dataToVerify: AuthChallenge = AuthChallenge.EMPTY
)

internal sealed interface VerifySsoUiEvent {
    data class Error(val message: String) : VerifySsoUiEvent
    data class NavigateAuthChallenge(val data: AuthChallenge) : VerifySsoUiEvent
}
