package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.callProtection.impl.R

@Composable
fun BlocklistSuccessDialog(
    onDismiss: () -> Unit,
    onGotItClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.width(345.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = DSSpacing.s6,
                            start = DSSpacing.s6,
                            end = DSSpacing.s6,
                            bottom = 0.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_alert_phishing),
                        contentDescription = "Success",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s4))

                    Text(
                        text = "Added to blocklist",
                        style = DSTypography.body1.bold, // 18.sp
                        color = DSColors.textHeading,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s2))

                    Text(
                        text = "This phone number has been added\nto your Family Blocklist so that all\nmembers are protected.",
                        style = DSTypography.caption1.regular, // 14.sp
                        color = DSColors.textNeutral,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DSSpacing.s6)
                ) {
                    Button(
                        onClick = onGotItClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DSColors.surfaceAction,
                            contentColor = DSColors.textInverted
                        )
                    ) {
                        Text(
                            text = "Got it",
                            style = DSTypography.body2.medium // 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun BlocklistSuccessDialogPreview() {
    BlocklistSuccessDialog({}) {

    }
}