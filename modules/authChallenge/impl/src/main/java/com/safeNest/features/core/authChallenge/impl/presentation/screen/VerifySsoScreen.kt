package com.safeNest.features.core.authChallenge.impl.presentation.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge
import com.safeNest.features.core.authChallenge.impl.domain.model.ChallengeParametersKey
import com.safeNest.features.core.authChallenge.impl.domain.model.ClientMetadataKey
import com.safeNest.features.core.authChallenge.impl.LoadingIndicator
import com.safeNest.features.core.authChallenge.impl.ObserveAsEvents
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Composable
internal fun VerifySsoScreen(
    viewModel: VerifySsoViewModel = hiltViewModel(),
    data: AuthChallenge,
    showSsoScreenUi: Boolean,
    onNavigateAuthChallenge: (AuthChallenge) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState = viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is VerifySsoUiEvent.NavigateAuthChallenge -> {
                onNavigateAuthChallenge(event.data)
            }

            is VerifySsoUiEvent.Error -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(showSsoScreenUi) {
        if (!showSsoScreenUi) {
            val clientMetadata = buildJsonObject {
                put(
                    ClientMetadataKey.ID_TOKEN,
                    getIdToken(context, data.challengeParameters)
                )
            }
            viewModel.verify(data.copy(clientMetadata = clientMetadata))
        }
    }

    Scaffold(
        Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (!showSsoScreenUi) return@Scaffold

        val screenName = "Verify SSO screen"

        Column(
            Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                screenName,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                data.toString(challengeParameters = true),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val clientMetadata = buildJsonObject {
                            put(
                                ClientMetadataKey.ID_TOKEN,
                                getIdToken(context, data.challengeParameters)
                            )
                            put(
                                ClientMetadataKey.MOCK_NEXT_CHALLENGE_NAME,
                                "VERIFY_OTP"
                            )
                        }
                        viewModel.verify(data.copy(clientMetadata = clientMetadata))
                    }
                }
            ) { Text("Verify token & Require next OTP") }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val clientMetadata = buildJsonObject {
                            put(
                                ClientMetadataKey.ID_TOKEN,
                                getIdToken(context, data.challengeParameters)
                            )
                        }
                        viewModel.verify(data.copy(clientMetadata = clientMetadata))
                    }
                }
            ) { Text("Verify token & Finish") }
        }
    }

    LoadingIndicator(uiState.value.isLoading)
}

private suspend fun getIdToken(
    context: Context,
    challengeParameters: JsonObject?,
): String {
    val serverClientId = challengeParameters?.get(ChallengeParametersKey.CLIENT_ID)
        ?.jsonPrimitive?.content ?: return ""
    val nonce = challengeParameters[ChallengeParametersKey.STATE]
        ?.jsonPrimitive?.content ?: return ""

    val credentialOption = GetSignInWithGoogleOption.Builder(serverClientId)
        .setNonce(nonce)
        .build()
    val credentialRequest =
        GetCredentialRequest.Builder().addCredentialOption(credentialOption).build()
    return runCatching {
        val result = CredentialManager.create(context).getCredential(context, credentialRequest)
        val credential = result.credential
        if (credential is CustomCredential) {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else {
                throw IllegalArgumentException("Invalid credential type: ${credential.type}")
            }
        } else {
            throw IllegalArgumentException("Invalid credential type: ${credential.type}")
        }
    }.getOrDefault("")
}