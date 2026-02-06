package com.safeNest.features.core.signIn.impl.presentation.screen

import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.features.core.authChallenge.api.presentation.router.AuthChallengeDeeplink
import com.safeNest.features.core.signIn.impl.LoadingIndicator
import com.safeNest.features.core.signIn.impl.ObserveAsEvents
import com.uney.core.router.compose.LocalRouterManager

@Composable
internal fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    val routerManager = LocalRouterManager.current

    val uiState = viewModel.uiState.collectAsState()

    val authChallengeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (RESULT_OK == result.resultCode) {
            onSignInSuccess()
        }
    }

    var showSsoScreenUi by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is SignInUiEvent.NavigateAuthChallenge -> {
                val intent = routerManager.getLaunchIntent(
                    AuthChallengeDeeplink.entryPoint(event.data, showSsoScreenUi)
                )
                intent?.let { authChallengeLauncher.launch(intent) }
            }

            is SignInUiEvent.Error -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(Modifier.fillMaxSize()) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Sign In screen",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = {
                    viewModel.signInEmail("bob@email.com")
                }
            ) { Text("Sign In Email") }

            Button(
                onClick = {
                    showSsoScreenUi = true
                    viewModel.signInSso()
                }
            ) { Text("Sign In SSO") }

            Button(
                onClick = {
                    showSsoScreenUi = false
                    viewModel.signInSso()
                }
            ) { Text("Sign In SSO (no UI)") }
        }
    }

    LoadingIndicator(uiState.value.isLoading)
}