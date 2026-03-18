package com.safeNest.demo.features.callProtection.impl.presentation.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist.BlocklistScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.WhitelistScreen
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

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
    tabName: String = "Blocklist",
    onBack: () -> Unit = {},
    onAddToWhitelist: () -> Unit = {},
    onAddToBlacklist: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.from(tabName)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header & Tabs Section
                Column(modifier = Modifier.background(DSColors.surface1)) {

                    Spacer(modifier = Modifier.statusBarsPadding())
                    Toolbar(
                        text = "Call Protection"
                    ) {
                        onBack()
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.width(DSSpacing.s6))
                        CustomTab(
                            title = "Blocklist",
                            isSelected = selectedTab == Tab.Blocklist,
                            onClick = { selectedTab = Tab.Blocklist },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(DSSpacing.s6))
                        CustomTab(
                            title = "Whitelist",
                            isSelected = selectedTab == Tab.Whitelist,
                            onClick = { selectedTab = Tab.Whitelist },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(DSSpacing.s6))
                    }
                }

                Crossfade(
                    targetState = selectedTab,
                    label = "Tab Transition",
                    modifier = Modifier.fillMaxSize().background(BackgroundLight)
                ) { tabIndex ->
                    when (tabIndex) {
                        Tab.Blocklist -> BlocklistScreen {
                            onAddToBlacklist()
                        }
                        Tab.Whitelist -> WhitelistScreen(paddingValues = paddingValues) {
                            onAddToWhitelist()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(DSSpacing.s3))
        Text(
            text = title,
            color = if (isSelected) DSColors.textAction else DSColors.textNeutral,
            style = if (isSelected) DSTypography.body2.bold else DSTypography.body2.medium
        )
        Spacer(modifier = Modifier.height(DSSpacing.s3))
        if (isSelected)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(DSColors.borderAction)
            )
    }
}

@Preview
@Composable
fun CallProtectionScreenPreview() {
    CallProtectionScreen()
}

enum class Tab(val tabName: String) {
    Blocklist("Blocklist"), Whitelist("Whitelist");

    companion object {
        fun from(tabName: String): Tab {
            return when (tabName) {
                "Blocklist" -> Blocklist
                "Whitelist" -> Whitelist
                else -> Blocklist
            }
        }
    }
}