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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.WhitelistViewModel

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

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            // Bottom section with the Add button and Cancel text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        viewModel.add(number = phoneNumber, name = name)
                        phoneNumber = ""
                        name = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Whitelist", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
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
                        text = "Add Allowed Contact",
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
                // Header Description (You probably want to change this copy!)
                Text(
                    text = "Define a pattern to automatically block matching incoming calls or messages. Use asterisks (*) as wildcards.",
                    color = TextDark,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Phone Number Input
                Text(
                    text = "Phone number",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Reusing the custom pill text field from the previous screen
                CustomPillTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = "+1 (555) 000-0000"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Phone Number Input
                Text(
                    text = "Name",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Reusing the custom pill text field from the previous screen
                CustomPillTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Name"
                )
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