package com.safeNest.demo.features.home.impl.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.home.impl.R

@Composable
fun CustomPromptScreen(
    onBackClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        // Top bar - Fixed/Anchored
        Surface(
            color = DSColors.surfacePrimary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = DSSpacing.s6, vertical = DSSpacing.s4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSSpacing.s4)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DSColors.surfacePrimary)
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = DSColors.textAction,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Configure AI Prompt",
                    style = DSTypography.h4.bold,
                    color = DSColors.textAction,
                )
            }
        }

        // Content - Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(DSSpacing.s6),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.s4)
        ) {
                Text(
                    text = "Your prompt must return valid JSON with this structure:",
                    style = DSTypography.caption1.regular,
                    color = DSColors.textBody,
                    lineHeight = 18.sp
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DSColors.surface2),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = """
                            {
                              "status": 0 | 1 | 2,
                              "reasons": [
                                {
                                  "title": "string",
                                  "description": "string"
                                }
                              ]
                            }
                        """.trimIndent(),
                        style = DSTypography.caption2.regular,
                        color = DSColors.textBody,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(DSSpacing.s4)
                    )
                }

                Text(
                    text = "• Use {message} for input text\n• Use {context} for context\n• Status: 0=Safe, 1=Scam, 2=Unverified",
                    style = DSTypography.caption2.regular,
                    color = DSColors.textBody.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(DSSpacing.s2))

                Text(
                    text = "Prompt Template",
                    style = DSTypography.caption1.semiBold,
                    color = DSColors.textBody
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = uiState.customPrompt,
                        onValueChange = settingsViewModel::updateCustomPrompt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
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
                        placeholder = {
                            Text(
                                text = "Enter your custom prompt...",
                                style = DSTypography.caption2.regular,
                                color = DSColors.textNeutral
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(DSSpacing.s4))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSSpacing.s3)
                ) {
                    DSButton(
                        text = "Reset to Default",
                        onClick = settingsViewModel::resetToDefaultPrompt,
                        modifier = Modifier.weight(1f),
                        textStyle = DSTypography.caption1.medium
                    )

                    DSButton(
                        text = "Save Changes",
                        onClick = {
                            settingsViewModel.saveCustomPrompt()
                            onBackClick()
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = DSTypography.caption1.medium
                    )
                }
        }
    }
}
