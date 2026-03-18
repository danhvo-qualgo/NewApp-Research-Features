package com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.WhitelistViewModel
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

// Reusing our custom colors
val PrimaryPurple = Color(0xFF5A4FCF)
val BackgroundLight = Color(0xFFEBEBFA)
val TextDark = Color(0xFF1E1E24)
val LightTextGray = Color(0xFFA0A0B0)

@Composable
fun AddWhitelistScreen(
    viewModel: WhitelistViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
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
                        text = "Add Allow Contact",
                        onActionClick = onBack
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(DSSpacing.s6)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Description (You probably want to change this copy!)
                    Text(
                        text = "Define a pattern to automatically block matching incoming calls or messages. Use asterisks (*) as wildcards.",
                        style = DSTypography.body2.medium,
                        color = DSColors.textBody
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s6))

                    Text(
                        text = "Phone number",
                        style = DSTypography.caption1.regular,
                        color = DSColors.textBody
                    )
                    Spacer(modifier = Modifier.height(DSSpacing.s3))

                    // Reusing the custom pill text field from the previous screen
                    CustomPillTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = "+1 (555) 000-0000"
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s6))

                    Text(
                        text = "Name",
                        style = DSTypography.caption1.regular,
                        color = DSColors.textBody
                    )
                    Spacer(modifier = Modifier.height(DSSpacing.s3))

                    CustomPillTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Hint name"
                    )
                }
                Spacer(modifier = Modifier.height(DSSpacing.s6))
                Button(
                    onClick = {
                        viewModel.add(name = name, number = phoneNumber)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = DSSpacing.s6, vertical = DSSpacing.s4),
                    contentPadding = PaddingValues(DSSpacing.s4),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DSColors.surfaceAction
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Icon(ImageVector.vectorResource(R.drawable.ic_plus), contentDescription = "Add")
                    Spacer(modifier = Modifier.width(DSSpacing.s3))
                    Text("Add to whitelist", style = DSTypography.body2.bold, color = DSColors.textOnAction)
                }
            }
        }
    }
}

    // Custom composable for the pill-shaped text field
    @Composable
    fun CustomPillTextField(
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DSColors.surface1, RoundedCornerShape(24.dp))
                .padding(horizontal = DSSpacing.s5, vertical = DSSpacing.s4)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = DSTypography.body2.regular.copy(color = DSColors.textBody),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = DSTypography.body2.regular,
                            color = DSColors.textNeutral
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
