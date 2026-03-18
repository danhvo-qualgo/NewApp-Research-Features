package com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist.BlocklistViewModel
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.add.CustomPillTextField
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import dagger.hilt.android.lifecycle.HiltViewModel

// Reusing our custom colors
val PrimaryPurple = Color(0xFF5A4FCF)
val BackgroundLight = Color(0xFFEBEBFA) // Slightly adjusted to match the bluish-purple tint
val TextDark = Color(0xFF1E1E24)
val TextGray = Color(0xFF6B6B7B)
val LightTextGray = Color(0xFFA0A0B0)
val InfoBoxBg = Color(0xFFF0F4FF)
val InfoBoxBorder = Color(0xFFD0D9F5)

@Composable
fun AddBlockPatternScreen(
    viewModel: BlocklistViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    // State for the text inputs
    var blockPattern by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
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
                        text = "Block Pattern",
                        style = DSTypography.caption1.regular,
                        color = DSColors.textBody
                    )
                    Spacer(modifier = Modifier.height(DSSpacing.s3))

                    // Reusing the custom pill text field from the previous screen
                    CustomPillTextField(
                        value = blockPattern,
                        onValueChange = { blockPattern = it },
                        placeholder = "e.g., +84* or 1900*"
                    )
                    Text(
                        text = "Example: +1-800* blocks all numbers starting with this prefix.",
                        color = DSColors.textNeutral,
                        style = DSTypography.caption2.regular,
                        modifier = Modifier.padding(horizontal = DSSpacing.s1)
                    )

                    Spacer(modifier = Modifier.height(DSSpacing.s6))

                    Text(
                        text = "Description",
                        style = DSTypography.caption1.regular,
                        color = DSColors.textBody
                    )
                    Spacer(modifier = Modifier.height(DSSpacing.s3))

                    CustomPillTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "e.g., Insurance spam or Telemarketing"
                    )
                    Spacer(modifier = Modifier.height(DSSpacing.s6))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DSColors.surfaceActive),
                        border = BorderStroke(1.dp, DSColors.surfaceActionDisabled),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(DSSpacing.s5)) {
                            Icon(ImageVector.vectorResource(R.drawable.ic_image_gallery), tint = Color.Unspecified, contentDescription = "Info")
                            Spacer(modifier = Modifier.width(DSSpacing.s3))
                            Text(text = "Pattern matching is case-insensitive and applies to all incoming communications from numbers matching this rule.",
                                color = DSColors.textBody,
                                style = DSTypography.caption1.regular,
                                lineHeight = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(DSSpacing.s6))
                }
                Spacer(modifier = Modifier.height(DSSpacing.s6))
                Button(
                    onClick = {
                        viewModel.add(blockPattern, description)
                        blockPattern = ""
                        description = ""
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
                    Text("Add to blocklist", style = DSTypography.body2.bold, color = DSColors.textOnAction)
                }
            }
        }
    }
}

// Custom composable to achieve that perfectly borderless, pill-shaped text field
@Composable
fun CustomPillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = TextDark,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(PrimaryPurple),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = LightTextGray,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
    }
}