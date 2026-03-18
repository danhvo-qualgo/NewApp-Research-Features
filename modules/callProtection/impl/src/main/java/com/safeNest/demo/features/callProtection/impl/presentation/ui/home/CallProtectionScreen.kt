package com.safeNest.demo.features.callProtection.impl.presentation.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist.BlocklistScreen
import com.safeNest.demo.features.callProtection.impl.presentation.ui.component.Toolbar
import com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist.WhitelistScreen
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun CallProtectionScreen(
    tabName: String,
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
                    modifier = Modifier.fillMaxSize().background(DSColors.surfaceActive)
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

enum class Tab(val tabName: String) {
    Blocklist(CallDetectionDeeplink.BLOCKLIST), Whitelist(CallDetectionDeeplink.WHITELIST);

    companion object {
        fun from(tabName: String): Tab {
            return when (tabName) {
                Blocklist.tabName -> Blocklist
                Whitelist.tabName -> Whitelist
                else -> Blocklist
            }
        }
    }
}