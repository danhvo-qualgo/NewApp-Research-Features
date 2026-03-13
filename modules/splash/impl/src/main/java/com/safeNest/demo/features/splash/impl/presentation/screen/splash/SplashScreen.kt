package com.safeNest.demo.features.splash.impl.presentation.screen.splash

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.baseApp.base.view.BaseScreen
import com.safeNest.demo.features.splash.impl.R
import com.uney.core.router.compose.LocalRouterManager

@Composable
internal fun SplashScreen(
    onNavigateToRequestPermission: () -> Unit,
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
            modifier = Modifier.fillMaxSize().background(colorResource(R.color.splash_background)),
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

                is UiState.Success -> {
                    LaunchedEffect(Unit) {

                        if((uiState as UiState.Success).allPermissionGranted) {
                            if (routerManager.navigate(activity, "internal://featureHome".toUri())) {
                                activity.finish()
                            }
                        } else {
                            onNavigateToRequestPermission()
                        }
                    }
                }

                else -> Unit
            }

            Spacer(Modifier.weight(1f))
        }
    }
}