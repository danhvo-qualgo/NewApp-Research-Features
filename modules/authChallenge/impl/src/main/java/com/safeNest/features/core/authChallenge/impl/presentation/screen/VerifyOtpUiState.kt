package com.safeNest.features.core.authChallenge.impl.presentation.screen

import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge

internal data class VerifyOtpUiState(
    val isLoading: Boolean = false,
    val dataToVerify: AuthChallenge = AuthChallenge.EMPTY
)

internal sealed interface VerifyOtpUiEvent {
    data class Error(val message: String) : VerifyOtpUiEvent
    data class NavigateAuthChallenge(val data: AuthChallenge) : VerifyOtpUiEvent
}