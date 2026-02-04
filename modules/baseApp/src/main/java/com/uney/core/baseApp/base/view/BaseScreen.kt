package com.uney.core.baseApp.base.view

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uney.core.baseApp.base.viewmodel.BaseUiEvent
import com.uney.core.baseApp.base.viewmodel.BaseUiState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BaseScreen(
    modifier: Modifier = Modifier,
    baseUiState: BaseUiState,
    baseUiEvent: SharedFlow<BaseUiEvent>,
    screenInfo: ScreenInfo = ScreenInfo(),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        if (baseUiState.loading) {
            CircularProgressIndicator()
        }

        content()
    }
}
/*
    buy - sell
    short_sell -> short_buy
 */