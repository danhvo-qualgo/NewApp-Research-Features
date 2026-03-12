package com.safeNest.demo.features.baseApp.splash

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.baseApp.R
import com.safeNest.demo.features.baseApp.base.view.BaseScreen
import com.uney.core.router.compose.LocalRouterManager

@Composable
internal fun LoadingScreen(
    entryPoint: Uri,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val routerManager = LocalRouterManager.current
    val activity = LocalActivity.current ?: return

    BaseScreen(
        modifier = Modifier,
        baseUiEvent = viewModel.baseUiEvent,
        baseUiState = viewModel.baseUiState.collectAsState().value,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.ic_splash_loading),
                contentDescription = null,
            )

            when (uiState) {
                UiState.Error -> {
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            activity,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                UiState.Success -> {
                    LaunchedEffect(Unit) {
                        if (routerManager.navigate(activity, entryPoint)) {
                            activity.finish()
                        } else {
                            Toast.makeText(
                                activity,
                                "Feature not available. Please provide correct entry point through @AppEntryPoint in hilt module.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                else -> Unit
            }

            Spacer(Modifier.weight(1f))
        }
    }
}