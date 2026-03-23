package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import androidx.core.net.toUri

@Composable
fun MakeCallConfirmScreen(
    callerIdInfo: CallerIdInfo,
    onGoToReview: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bgGradient = gradientBackground

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgGradient)
            .systemBarsPadding(),
        topBar = {
            Column(modifier = Modifier.background(DSColors.surface1)) {
                Spacer(modifier = Modifier.statusBarsPadding())
                Toolbar(
                    text = "",
                    onActionClick = onBack
                )
            }
        },
        bottomBar = {
            DockBarActions() {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:${callerIdInfo.phoneNumber}".toUri()
                }
                context.startActivity(intent)
                onGoToReview()
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DSSpacing.s10))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(DSColors.surfaceAction, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield",
                    tint = DSColors.iconInverted,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(DSSpacing.s6))

            Text(
                text = "Safety First",
                style = DSTypography.h3.bold,
                color = DSColors.textHeading
            )

            Spacer(modifier = Modifier.height(DSSpacing.s2))

            Text(
                text = "Ask for purpose only\nAvoid sharing personal information.\nRecord and send for scam review.",
                style = DSTypography.body1.regular,
                color = DSColors.textBody,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DSSpacing.s8))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DSSpacing.s6)
                    .background(DSColors.surfaceSuccessLightest, RoundedCornerShape(24.dp))
                    .padding(DSSpacing.s5)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(DSSpacing.s6)
                            .background(DSColors.surfaceSuccess, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Check",
                            tint = DSColors.iconInverted,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(DSSpacing.s3))

                    Text(
                        text = "SafeNest automatically detects unusual patterns in real-time during your calls.",
                        style = DSTypography.caption1.medium,
                        color = DSColors.textBody
                    )
                }
            }
        }
    }
}

@Composable
private fun DockBarActions(
    onMakeCall: () -> Unit = {}
) {
    Surface(
        color = DSColors.surface1.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DSSpacing.s6, vertical = DSSpacing.s4),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onMakeCall,
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
                    text = "Make the call",
                    style = DSTypography.body2.bold,
                    color = DSColors.textInverted
                )
            }

            Spacer(modifier = Modifier.height(DSSpacing.s2))

            TextButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape
            ) {
                Text(
                    text = "How to record the call",
                    style = DSTypography.body2.bold,
                    color = DSColors.textHeading
                )
            }
        }
    }
}



@Composable
@Preview
fun MakeCallConfirmScreenPreview() {
//    MakeCallConfirmScreen {
//
//    }
}