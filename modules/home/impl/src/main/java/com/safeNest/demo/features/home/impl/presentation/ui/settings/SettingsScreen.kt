package com.safeNest.demo.features.home.impl.presentation.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.component.DSDropdown
import com.safeNest.demo.features.designSystem.component.DsToggle
import com.safeNest.demo.features.designSystem.component.DsToggleSize
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.urlGuard.api.TelegramLink

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
            .verticalScroll(rememberScrollState())
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

            Spacer(modifier = Modifier.height(DSSpacing.s4))
            FormCheckEnableCard(
                isEnabled = uiState.isFormCheckEnabled,
                onEnableChange = { enabled ->
                    settingsViewModel.toggleFormCheck(enabled)
                }
            )

            Spacer(modifier = Modifier.height(DSSpacing.s4))
            SetTelegramLinkCard(
                link = TelegramLink.telegramLink,
                onLinkChange = {
                    TelegramLink.telegramLink = it
                }
            )

            Spacer(modifier = Modifier.height(DSSpacing.s4))

            DownloadModelCard(
                isDownloading = uiState.isDownloadingModel,
                isDeleting = uiState.isDeletingModel,
                downloadProgress = uiState.modelDownloadProgress,
                modelAlreadyOnDisk = uiState.modelAlreadyOnDisk,
                isModelDownloaded = uiState.isModelDownloaded,
                onDownloadClick = settingsViewModel::downloadModel,
                onDeleteClick = settingsViewModel::deleteModel
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

@Composable
private fun FormCheckEnableCard(
    isEnabled: Boolean,
    onEnableChange: (Boolean) -> Unit
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
            Row(
                modifier =  Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Form Check",
                    style = DSTypography.caption1.regular,
                    color = DSColors.textBody.copy(alpha = 0.7f)
                )

                DsToggle(
                    checked = isEnabled,
                    onCheckedChange = onEnableChange,
                    size = DsToggleSize.MD
                )
            }

            Text(
                text = "Enable sensitive form inspection before URL reputation check",
                style = DSTypography.caption1.regular,
                color = DSColors.textBody,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SetTelegramLinkCard(
    link: String,
    onLinkChange: (String) -> Unit
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
                text = "Set telegram bot link",
                style = DSTypography.caption1.regular,
                color = DSColors.textBody.copy(alpha = 0.7f)
            )

            TextField(
                value = link,
                onValueChange = onLinkChange,
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = DSTypography.caption2.regular.copy(
                    fontFamily = FontFamily.Monospace
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DSColors.surface1,
                    unfocusedContainerColor = DSColors.surface1,
                    focusedIndicatorColor = DSColors.borderAction,
                    unfocusedIndicatorColor = DSColors.borderPrimary,
                    focusedTextColor = DSColors.textBody,
                    unfocusedTextColor = DSColors.textBody
                ),
            )
        }
    }
}

@Composable
private fun DownloadModelCard(
    isDownloading: Boolean,
    isDeleting: Boolean,
    downloadProgress: Int,
    modelAlreadyOnDisk: Boolean,
    isModelDownloaded: Boolean,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                text = "On-device model",
                style = DSTypography.caption1.semiBold,
                color = DSColors.textBody
            )

            Text(
                text = "Download the AI model for on-device analysis. Required once before local analysis can run.",
                style = DSTypography.caption2.regular,
                color = DSColors.textBody.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )

            if (!isDownloading && isModelDownloaded) {
                Text(
                    text = "Model download completed",
                    style = DSTypography.caption2.medium,
                    color = DSColors.textActionActive,
                )
            }

            if (isDeleting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DSColors.textActionActive,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(DSSpacing.s3))
                    Text(
                        text = "Deleting model…",
                        style = DSTypography.body2.medium,
                        color = DSColors.textBody
                    )
                }
            } else if (isDownloading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DSSpacing.s3),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (modelAlreadyOnDisk) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = DSColors.textActionActive,
                            trackColor = DSColors.borderPrimary.copy(alpha = 0.35f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DSColors.textActionActive,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(DSSpacing.s3))
                            Column {
                                Text(
                                    text = "Model download completed",
                                    style = DSTypography.body2.medium,
                                    color = DSColors.textBody
                                )
                                Text(
                                    text = "Loading model…",
                                    style = DSTypography.caption2.regular,
                                    color = DSColors.textBody.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LinearProgressIndicator(
                            progress = { downloadProgress.coerceIn(0, 100) / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = DSColors.textActionActive,
                            trackColor = DSColors.borderPrimary.copy(alpha = 0.35f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DSColors.textActionActive,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(DSSpacing.s3))
                            Text(
                                text = "Downloading… ${downloadProgress.coerceIn(0, 100)}%",
                                style = DSTypography.body2.medium,
                                color = DSColors.textBody
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSSpacing.s3)
                ) {
                    DSButton(
                        text = "Download",
                        onClick = onDownloadClick,
                        modifier = Modifier.weight(1f),
                        textStyle = DSTypography.body2.medium
                    )
                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.weight(1f),
                        enabled = isModelDownloaded,
                        border = BorderStroke(1.dp, DSColors.borderError),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DSColors.textError,
                            disabledContentColor = DSColors.textDisabled.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Delete",
                            style = DSTypography.body2.medium
                        )
                    }
                }
            }
        }
    }
}
