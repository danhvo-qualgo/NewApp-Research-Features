package net.qualgo.safeNest.core.signIn.impl.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.qualgo.safeNest.core.authChallenge.api.domain.useCase.InitSignInEmailUseCase
import net.qualgo.safeNest.core.authChallenge.api.domain.useCase.InitSignInSsoUseCase
import com.uney.core.utils.kotlin.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val initSignInEmailUseCase: InitSignInEmailUseCase,
    private val initSignInSsoUseCase: InitSignInSsoUseCase
) : ViewModel() {

    private var _uiState = MutableStateFlow(SignInUiState())
    val uiState = _uiState.asStateFlow()

    private var _uiEvent = Channel<SignInUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun signInEmail(email: String) {
        signIn { initSignInEmailUseCase(email) }
    }

    fun signInSso() {
        signIn { initSignInSsoUseCase() }
    }

    private fun signIn(block: suspend () -> DomainResult<JsonObject, String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = block()) {
                is DomainResult.Error<String> -> {
                    _uiEvent.send(SignInUiEvent.Error(result.error))
                }

                is DomainResult.Success<JsonObject> -> {
                    _uiEvent.send(SignInUiEvent.NavigateAuthChallenge(result.data))
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}