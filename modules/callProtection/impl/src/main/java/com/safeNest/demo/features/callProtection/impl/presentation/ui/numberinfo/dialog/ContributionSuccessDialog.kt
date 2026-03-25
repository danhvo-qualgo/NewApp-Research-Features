package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun ContributionSuccessDialog(
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
            modifier = Modifier.fillMaxWidth(),
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
                        imageVector = ImageVector.vectorResource(R.drawable.ic_alert_safe),
                        contentDescription = "Safe",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s4))

                    Text(
                        text = "Thank you for\ncontribution",
                        style = DSTypography.body1.bold,
                        color = DSColors.textHeading,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s2))

                    Text(
                        text = "The phone number is now marked\nas known for family members.",
                        style = DSTypography.caption1.regular,
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
                            style = DSTypography.body2.medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun ContributionSuccessDialogPreview() {
    ContributionSuccessDialog({}) {

    }
}