package net.qualgo.safeNest.core.authChallenge.impl.presentation.screen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.ClientMetadataKey
import net.qualgo.safeNest.core.authChallenge.impl.LoadingIndicator
import net.qualgo.safeNest.core.authChallenge.impl.ObserveAsEvents
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Locale
import kotlin.random.Random

@Composable
internal fun VerifyOtpScreen(
    viewModel: VerifyOtpViewModel = hiltViewModel(),
    data: AuthChallenge,
    onNavigateHelp: () -> Unit,
    onNavigateAuthChallenge: (AuthChallenge) -> Unit
) {
    val context = LocalContext.current

    val uiState = viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is VerifyOtpUiEvent.NavigateAuthChallenge -> {
                onNavigateAuthChallenge(event.data)
            }

            is VerifyOtpUiEvent.Error -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(Modifier.fillMaxSize()) { paddingValues ->
        val screenName = "Verify OTP screen"

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
                onClick = onNavigateHelp
            ) {
                Text("Help")
            }

            Button(
                onClick = {
                    val clientMetadata = buildJsonObject {
                        put(ClientMetadataKey.CODE, generateOtp())
                        put(
                            ClientMetadataKey.MOCK_NEXT_CHALLENGE_NAME,
                            "VERIFY_OTP"
                        )
                    }
                    viewModel.verify(data.copy(clientMetadata = clientMetadata))
                }
            ) { Text("Verify OTP & Require next OTP") }

            Button(
                onClick = {
                    val clientMetadata = buildJsonObject {
                        put(ClientMetadataKey.CODE, generateOtp())
                    }
                    viewModel.verify(data.copy(clientMetadata = clientMetadata))
                }
            ) { Text("Verify OTP & Finish") }
        }
    }

    LoadingIndicator(uiState.value.isLoading)
}

private fun generateOtp(): String {
    val randomPin = Random.nextInt(0, 1000000)
    return String.format(Locale.getDefault(), "%06d", randomPin)
}