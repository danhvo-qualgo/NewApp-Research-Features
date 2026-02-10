package net.qualgo.safeNest.core.signIn.impl.presentation.screen

import kotlinx.serialization.json.JsonObject

internal data class SignInUiState(
    val isLoading: Boolean = false
)

internal sealed interface SignInUiEvent {
    data class Error(val message: String) : SignInUiEvent
    data class NavigateAuthChallenge(val data: JsonObject) : SignInUiEvent
}