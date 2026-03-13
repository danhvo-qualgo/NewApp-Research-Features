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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    onBack: () -> Unit
) {
    // State for the text inputs
    var blockPattern by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundLight, // The main body has this light tinted background
        bottomBar = {
            // White background container for the bottom button
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section (White background)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryPurple,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                onBack()
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Add Block Pattern",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPurple
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Content Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Description
                Text(
                    text = "Define a pattern to automatically block matching incoming calls or messages. Use asterisks (*) as wildcards.",
                    color = TextDark,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Block Pattern Input
                Text(
                    text = "Block Pattern",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomPillTextField(
                    value = blockPattern,
                    onValueChange = { blockPattern = it },
                    placeholder = "e.g., +84* or 1900*"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Example: +1-800* blocks all numbers starting with this prefix.",
                    color = LightTextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Description Input
                Text(
                    text = "Description",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomPillTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "e.g., Insurance spam or Telemarketing"
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Info Box
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = InfoBoxBg),
                    border = BorderStroke(1.dp, InfoBoxBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser, // Using a similar shield icon
                            contentDescription = "Info",
                            tint = PrimaryPurple,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pattern matching is case-insensitive and applies to all incoming communications from numbers matching this rule.",
                            color = TextDark,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp)) // Extra padding for scrolling
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