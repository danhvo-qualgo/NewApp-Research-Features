package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.impl.domain.common.formatBeautifulNumber
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog.AddToSafeListDialog
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog.BlocklistSuccessDialog
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog.ContributionSuccessDialog
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog.DSCheckbox
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun ReviewCallScreen(
    callerIdInfo: CallerIdInfo,
    missingCallViewModel: MissingCallViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {

    var showDialog by remember { mutableStateOf(ReviewCallDialogState.HIDE) }

    val bgGradient = gradientBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgGradient)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.background(DSColors.surface1)) {

                Spacer(modifier = Modifier.statusBarsPadding())
                Toolbar(
                    text = "Back to Activity",
                    onActionClick = onBack
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(DSColors.surface2, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = DSColors.iconAction,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(DSSpacing.s6))

                Text(
                    text = "How was the call?",
                    style = DSTypography.h3.bold,
                    color = DSColors.textHeading,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(DSSpacing.s2))

                Text(
                    text = "Was ${formatBeautifulNumber(callerIdInfo.phoneNumber)} safe?",
                    style = DSTypography.body1.regular,
                    color = DSColors.textBody,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(DSSpacing.s6))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FeedbackOptionCard(
                        title = "This is spam/scam",
                        iconVector = Icons.Default.Block,
                        iconTint = DSColors.iconError,
                        iconBg = DSColors.surfaceErrorLightest,
                        onClick = {
                            missingCallViewModel.addToBlocklist(callerIdInfo.phoneNumber)
                            showDialog == ReviewCallDialogState.ADD_TO_BLOCKLIST_SUCCESS
                        }
                    )

                    FeedbackOptionCard(
                        title = "I know this person",
                        iconVector = Icons.Default.Person,
                        iconTint = DSColors.iconAction,
                        iconBg = Color(0xFFEEF2FF),
                        onClick = {
                            missingCallViewModel.addToWhitelist(callerIdInfo.phoneNumber, callerIdInfo.label)
                            showDialog == ReviewCallDialogState.ADD_TO_WHITELIST_SUCCESS
                        }
                    )

                    FeedbackOptionCard(
                        title = "I don't know",
                        iconVector = Icons.Default.HelpOutline,
                        iconTint = DSColors.iconAction,
                        iconBg = Color(0xFFEEF2FF),
                        onClick = {
                            onBack()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(DSSpacing.s6))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSCheckbox(
                        checked = true,
                        onCheckedChange = {

                        }
                    )

                    Spacer(modifier = Modifier.width(DSSpacing.s3))

                    Text(
                        text = "Contribute to community intelligence",
                        style = DSTypography.caption1.medium,
                        color = DSColors.textBody
                    )
                }
            }
        }
        if (showDialog == ReviewCallDialogState.ADD_TO_BLOCKLIST_SUCCESS) {
            BlocklistSuccessDialog(
                onDismiss = {
                },
                onGotItClick = {
                    onBack()
                }
            )
        }
        if (showDialog == ReviewCallDialogState.ADD_TO_WHITELIST_SUCCESS) {
            ContributionSuccessDialog(
                onDismiss = {
                },
                onGotItClick = {
                    onBack()
                }
            )
        }
    }
}

@Composable
private fun FeedbackOptionCard(
    title: String,
    iconVector: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = DSTypography.body2.bold,
                color = DSColors.textHeading,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = DSColors.iconNeutral,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

enum class ReviewCallDialogState {
    ADD_TO_BLOCKLIST_SUCCESS,
    ADD_TO_WHITELIST_SUCCESS,
    HIDE
}

@Preview(showBackground = true)
@Composable
fun ReviewCallScreenPreview() {
    ReviewCallScreen(callerIdInfo = CallerIdInfo("", "", CallerIdInfoType.SCAM))
}