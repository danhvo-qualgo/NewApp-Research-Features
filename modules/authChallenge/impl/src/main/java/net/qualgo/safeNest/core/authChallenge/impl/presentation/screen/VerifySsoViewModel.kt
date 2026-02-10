package net.qualgo.safeNest.core.authChallenge.impl.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge
import net.qualgo.safeNest.core.authChallenge.impl.domain.useCase.ProcessAuthChallengeUseCase
import com.uney.core.utils.kotlin.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class VerifySsoViewModel @Inject constructor(
    private val processAuthChallengeUseCase: ProcessAuthChallengeUseCase
) : ViewModel() {

    private var _uiState = MutableStateFlow(VerifySsoUiState())
    val uiState = _uiState.asStateFlow()

    private var _uiEvent = Channel<VerifySsoUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun verify(data: AuthChallenge) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, dataToVerify = data) }

            when (val result = processAuthChallengeUseCase(data)) {
                is DomainResult.Error<String> -> {
                    _uiEvent.send(VerifySsoUiEvent.Error(result.error))
                }

                is DomainResult.Success<AuthChallenge> -> {
                    _uiEvent.send(VerifySsoUiEvent.NavigateAuthChallenge(result.data))
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}