package com.safeNest.demo.features.callProtection.impl.presentation.ui.home
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist.BlocklistScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.WhitelistScreen

// Custom Colors
val PrimaryPurple = Color(0xFF5A4FCF)
val BackgroundLight = Color(0xFFF0F0FA)
val TextDark = Color(0xFF1E1E24)
val TextGray = Color(0xFF8B8B9B)
val RedIconBg = Color(0xFFFFEAEA)
val RedIconColor = Color(0xFFD3405B)
val InfoBoxBg = Color(0xFFF0F4FF)
val InfoBoxBorder = Color(0xFFD0D9F5)
val PurpleIconBg = Color(0xFFF0F0FA)

@Composable
fun CallProtectionScreen(
    onBack: () -> Unit = {},
    onAddToWhitelist: () -> Unit = {},
    onAddToBlacklist: () -> Unit = {}
) {
    // State to track which tab is currently selected (0 = Blocklist, 1 = Whitelist)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Button(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> {
                            onAddToBlacklist()
                        }
                        1 -> {
                            onAddToWhitelist()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header & Tabs Section
            Column(modifier = Modifier.background(Color.White)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(28.dp).clickable {
                            onBack()
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Call protection",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    CustomTab(
                        title = "Blocklist",
                        isSelected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    CustomTab(
                        title = "Whitelist",
                        isSelected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Crossfade for smooth animation when switching tabs
            Crossfade(
                targetState = selectedTabIndex,
                label = "Tab Transition",
                modifier = Modifier.fillMaxSize().background(BackgroundLight)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> BlocklistScreen()
                    1 -> WhitelistScreen()
                }
            }
        }
    }
}

@Composable
fun CustomTab(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = if (isSelected) PrimaryPurple else TextGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (isSelected) PrimaryPurple else Color.Transparent)
        )
    }
}

@Preview
@Composable
fun CallProtectionScreenPreview() {
    CallProtectionScreen()
}