package com.safeNest.demo.baseApp.base.view

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BaseScreen(
    modifier: Modifier = Modifier,
    baseUiState: com.safeNest.demo.baseApp.base.viewModel.BaseUiState,
    baseUiEvent: SharedFlow<com.safeNest.demo.baseApp.base.viewModel.BaseUiEvent>,
    screenInfo: com.safeNest.demo.baseApp.base.view.ScreenInfo = _root_ide_package_.com.safeNest.demo.baseApp.base.view.ScreenInfo(),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        if (baseUiState.loading) {
            CircularProgressIndicator()
        }

        content()
    }
}
