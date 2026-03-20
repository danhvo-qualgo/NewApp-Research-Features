package com.safeNest.demo.features.home.impl.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeNest.demo.features.designSystem.component.DSDropdown
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(start = DSSpacing.s6, end = DSSpacing.s6, top = DSSpacing.s9),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        Text(
            text = "Settings",
            style = DSTypography.h2.bold,
            color = DSColors.textActionActive
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Scam Analyzer",
            style = DSTypography.caption1.regular,
            color = DSColors.textBody,
            modifier = Modifier.padding(top = DSSpacing.s6, bottom = DSSpacing.s2)
        )

        if (uiState.isLoading) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = DSColors.textActionActive
                    )
                }
            }
        } else {
            AnalyzeModeCard(
                selectedMode = if (uiState.isRemoteMode) AnalysisMode.Remote else AnalysisMode.OnDevice,
                onModeChange = { mode ->
                    settingsViewModel.toggleAnalyzeMode(mode == AnalysisMode.Remote)
                }
            )
        }
    }
}

enum class AnalysisMode {
    Remote,
    OnDevice
}

fun AnalysisMode.getDisplayName(): String {
    return when (this) {
        AnalysisMode.Remote -> "Remote"
        AnalysisMode.OnDevice -> "On Device"
    }
}

fun AnalysisMode.getDescription(): String {
    return when (this) {
        AnalysisMode.Remote -> "Analysis is performed on cloud servers with higher accuracy and speed."
        AnalysisMode.OnDevice -> "Analysis is performed locally on your device for better privacy."
    }
}

@Composable
private fun AnalyzeModeCard(
    selectedMode: AnalysisMode,
    onModeChange: (AnalysisMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.s3)
        ) {
            Text(
                text = "Analysis Mode",
                style = DSTypography.caption1.regular,
                color = DSColors.textBody.copy(alpha = 0.7f)
            )

            DSDropdown(
                selectedValue = selectedMode,
                options = listOf(AnalysisMode.Remote, AnalysisMode.OnDevice),
                onValueChange = onModeChange,
                getDisplayText = { it.getDisplayName() }
            )

            Text(
                text = selectedMode.getDescription(),
                style = DSTypography.caption1.regular,
                color = DSColors.textBody,
                lineHeight = 20.sp
            )
        }
    }
}
