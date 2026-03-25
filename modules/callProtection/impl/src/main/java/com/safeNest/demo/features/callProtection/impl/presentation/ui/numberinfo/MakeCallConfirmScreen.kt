package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo

import android.content.Intent
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

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
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgGradient),
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
            Column(modifier = Modifier.background(DSColors.surface1)) {
                Spacer(modifier = Modifier.statusBarsPadding())
                Toolbar(
                    text = "",
                    onActionClick = onBack
                )
            }

            Spacer(modifier = Modifier.height(DSSpacing.s10))

            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_safenest_shield_large),
                contentDescription = "Shield",
                tint = Color.Unspecified,
                modifier = Modifier.size(64.dp)
            )

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
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle_green),
                        contentDescription = "Check",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(DSSpacing.s3))

                    Text(
                        text = "KinShield automatically detects unusual patterns in real-time during your calls.",
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
                    imageVector = ImageVector.vectorResource(R.drawable.ic_phone_outline),
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
    MakeCallConfirmScreen(
        callerIdInfo = CallerIdInfo("", "", CallerIdInfoType.SPAM), {}
    ) {

    }
}