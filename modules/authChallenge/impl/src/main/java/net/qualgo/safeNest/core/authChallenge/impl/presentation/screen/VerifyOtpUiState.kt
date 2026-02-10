package net.qualgo.safeNest.core.authChallenge.impl.presentation.screen

import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge

internal data class VerifyOtpUiState(
    val isLoading: Boolean = false,
    val dataToVerify: AuthChallenge = AuthChallenge.EMPTY
)

internal sealed interface VerifyOtpUiEvent {
    data class Error(val message: String) : VerifyOtpUiEvent
    data class NavigateAuthChallenge(val data: AuthChallenge) : VerifyOtpUiEvent
}