package com.safeNest.demo.features.callProtection.impl.presentation.ui.numberinfo.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun AddToSafeListDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Boolean) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var isContributeChecked by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DSColors.surfaceSuccessLightest),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(
                            RoundedCornerShape(24.dp)
                        )
                        .background(DSColors.surface1),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DSSpacing.s6),
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
                            text = "Add to SafeList",
                            style = DSTypography.body1.bold,
                            color = DSColors.textHeading,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(DSSpacing.s2))

                        Text(
                            text = "This phone number has been added\nto your Family Blocklist so that all\nmembers are protected.",
                            style = DSTypography.caption1.regular,
                            color = DSColors.textNeutral,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(DSSpacing.s6))

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Give it a name to identify in the future",
                                style = DSTypography.caption1.medium,
                                color = DSColors.textBody
                            )

                            Spacer(modifier = Modifier.height(DSSpacing.s2))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        border = BorderStroke(1.dp, DSColors.borderInput),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .background(DSColors.surface1, RoundedCornerShape(24.dp))
                                    .padding(horizontal = DSSpacing.s5),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    textStyle = DSTypography.body2.regular.copy(color = DSColors.textBody),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (nameInput.isEmpty()) {
                                            Text(
                                                text = "e.g. Grandma's Doctor",
                                                style = DSTypography.body2.regular,
                                                color = DSColors.textNeutral
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = DSSpacing.s6,
                                end = DSSpacing.s6,
                                bottom = DSSpacing.s6
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = DSSpacing.s6),
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

                        Button(
                            onClick = { onSubmit(nameInput, isContributeChecked) },
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
                                text = "Submit",
                                style = DSTypography.body2.medium
                            )
                        }

                        Spacer(modifier = Modifier.height(DSSpacing.s3))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(DSColors.surface2, CircleShape),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "Cancel",
                                style = DSTypography.body2.medium,
                                color = DSColors.textHeading
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(DSSpacing.s6))
                Text(
                    text = "Community Trust Level - 85%",
                    style = DSTypography.caption1.bold,
                    color = DSColors.textSuccess,
                    modifier = Modifier.padding(horizontal = DSSpacing.s6)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Generally considered safe by 2,400+ users",
                    style = DSTypography.caption2.regular,
                    color = DSColors.textNeutral,
                    modifier = Modifier.padding(horizontal = DSSpacing.s6)
                )
                Spacer(modifier = Modifier.height(DSSpacing.s6))
            }
        }
    }
}

@Composable
@Preview
fun AddToSafeListDialogPreview() {
    AddToSafeListDialog({}) { _, _ ->

    }
}