package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.domain.common.formatBeautifulNumber
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog.AddToSafeListDialog
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog.BlocklistSuccessDialog
import com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog.ContributionSuccessDialog
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun MissingCallScreen(
    missingCallViewModel: MissingCallViewModel = hiltViewModel(),
    callerIdInfo: CallerIdInfo,
    onCallback: () -> Unit,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(MissingCallDialogState.HIDE) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(DSSpacing.s6))
                    ScamAlertCard(
                        callerIdInfo,
                        onAddToBlocklist = {
                            missingCallViewModel.addToBlocklist(callerIdInfo.phoneNumber)
                            showDialog = MissingCallDialogState.ADD_TO_BLOCKLIST_SUCCESS
                        },
                        onAddToWhitelist = {
                            showDialog = MissingCallDialogState.ADD_TO_WHITELIST
                        },
                        onCallback = onCallback
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s6))
                    CommunityIntelligenceSection()
                    Spacer(modifier = Modifier.height(DSSpacing.s6))
                }
            }
        }
        if (showDialog == MissingCallDialogState.ADD_TO_BLOCKLIST_SUCCESS) {
            BlocklistSuccessDialog(
                onDismiss = {
                },
                onGotItClick = {
                    onBack()
                }
            )
        }
        if (showDialog == MissingCallDialogState.ADD_TO_WHITELIST) {
            AddToSafeListDialog(
                onDismiss = {

                },
                onSubmit = { name, _ ->
                    missingCallViewModel.addToWhitelist(callerIdInfo.phoneNumber, name)
                    showDialog = MissingCallDialogState.ADD_TO_WHITELIST_SUCCESS
                }
            )
        }
        if (showDialog == MissingCallDialogState.ADD_TO_WHITELIST_SUCCESS) {
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
fun ScamAlertCard(
    callerIdInfo: CallerIdInfo,
    onAddToBlocklist: () -> Unit = {},
    onAddToWhitelist: () -> Unit = {},
    onCallback: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DSSpacing.s6),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DSColors.borderError),
        colors = CardDefaults.cardColors(containerColor = DSColors.borderError)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = DSSpacing.s3, end = 1.dp, top = 1.dp, bottom = 1.dp),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                bottomStart = 12.dp,
                topEnd = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = DSColors.surface1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DSSpacing.s5),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(DSSpacing.s9)
                        .background(DSColors.surfaceErrorLightest, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Blocked",
                        tint = DSColors.iconError,
                        modifier = Modifier.size(DSSpacing.s6)
                    )
                }

                Spacer(modifier = Modifier.height(DSSpacing.s3))

                Text(
                    text = callerIdInfo.label,
                    style = DSTypography.caption1.bold,
                    color = DSColors.textError
                )

                Spacer(modifier = Modifier.height(DSSpacing.s2))

                Text(
                    text = formatBeautifulNumber(callerIdInfo.phoneNumber),
                    style = DSTypography.h3.bold,
                    color = DSColors.textHeading
                )

                Spacer(modifier = Modifier.height(DSSpacing.s2))

                Text(
                    text = "This call was automatically intercepted and moved to the digital vault\nto protect your privacy.",
                    style = DSTypography.body2.regular,
                    color = DSColors.textNeutral,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(DSSpacing.s6))

                Button(
                    onClick = onAddToBlocklist,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DSSpacing.s9),
                    colors = ButtonDefaults.buttonColors(containerColor = DSColors.surfaceError),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Add to blocklist",
                        style = DSTypography.body2.semiBold,
                        color = DSColors.textInverted
                    )
                }

                Spacer(modifier = Modifier.height(DSSpacing.s4))

                TextButton(
                    onClick = onAddToWhitelist,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DSSpacing.s9)
                        .background(DSColors.surface2, CircleShape),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Add to Safelist",
                        style = DSTypography.body2.semiBold,
                        color = DSColors.textHeading
                    )
                }

                Spacer(modifier = Modifier.height(DSSpacing.s4))

                Button(
                    onClick = onCallback,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DSColors.surfaceAction),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = DSColors.iconInverted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(DSSpacing.s2))
                    Text(
                        text = "Callback",
                        style = DSTypography.body2.bold,
                        color = DSColors.textInverted
                    )
                }
            }
        }
    }

}

@Composable
fun CommunityIntelligenceSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DSSpacing.s6)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = DSSpacing.s4)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_user),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(DSSpacing.s5)
            )
            Spacer(modifier = Modifier.width(DSSpacing.s2))
            Text(
                text = "Community Intelligence",
                style = DSTypography.caption1.semiBold,
                color = DSColors.textHeading
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(DSSpacing.s6)
            ) {
                InfoRow(
                    icon = ImageVector.vectorResource(R.drawable.ic_alert_info),
                    title = "500+ active report",
                    description = "Verified community reports from authoritative sources like bcc.gov.vn and chongluadao.vn confirm this number as a known financial scam operation."
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = DSSpacing.s4),
                    color = DSColors.surfaceDivider
                )

                InfoRow(
                    icon = ImageVector.vectorResource(R.drawable.ic_alert_info),
                    title = "92% risk level",
                    description = "Sentinel Threat Rating"
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, title: String, description: String) {
    Row {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DSColors.iconError,
            modifier = Modifier.size(DSSpacing.s6)
        )
        Spacer(modifier = Modifier.width(DSSpacing.s4))
        Column {
            Text(
                text = title,
                style = DSTypography.body2.semiBold,
                color = DSColors.textHeading
            )
            Spacer(modifier = Modifier.height(DSSpacing.s1))
            Text(
                text = description,
                style = DSTypography.caption1.regular,
                color = DSColors.textNeutral
            )
        }
    }
}

enum class MissingCallDialogState {
    ADD_TO_BLOCKLIST_SUCCESS,
    ADD_TO_WHITELIST,
    ADD_TO_WHITELIST_SUCCESS,
    HIDE
}

@Composable
@Preview
fun BlockedScamCallScreenPreview() {
//    BlockedScamCallScreen(callerIdInfo = CallerIdInfo(
//        phoneNumber = "+1 (555) 012-3456",
//        label = "John Doe",
//        type = CallerIdInfoType.SCAM
//    ), {}, {}, {}) {
//
//    }
}