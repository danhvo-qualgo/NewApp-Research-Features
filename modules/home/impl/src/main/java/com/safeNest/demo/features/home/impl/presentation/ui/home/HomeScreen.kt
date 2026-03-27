package com.safeNest.demo.features.home.impl.presentation.ui.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.component.DsToggle
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.home.impl.R
import com.safeNest.demo.features.home.impl.presentation.ui.settings.SettingsScreen
import com.safeNest.demo.features.home.impl.presentation.ui.tool.ScamAnalyzerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onManageProtectionClick: () -> Unit,
    onRecordAudioClick: () -> Unit = {},
    onUploadAudioClick: (Uri) -> Unit = {},
    onUploadImageClick: (Uri) -> Unit = {},
    onBlocklistClick: () -> Unit,
    onWhitelistClick: () -> Unit,
    onScamAnalyzerClick: (String) -> Unit,
    onConsumeSharedText: () -> Unit = {},
    currentTab: MutableState<BottomTab>,
    sharedText: String? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                SafeNestBottomNavigation(
                    onHomeClick = { currentTab.value = BottomTab.Home },
                    onToolsClick = { currentTab.value = BottomTab.Tools },
                    onSettingsClick = { currentTab.value = BottomTab.Settings },
                    currentTab.value
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (currentTab.value) {
                    BottomTab.Home -> {
                        SafeNestHomeScreen(
                            innerPadding,
                            onBlocklistClick = onBlocklistClick,
                            onWhitelistClick = onWhitelistClick,
                            onManageProtectionClick = onManageProtectionClick
                        )
                    }

                    BottomTab.Tools -> {
                        ScamAnalyzerScreen(
                            onScamAnalyzerClick = onScamAnalyzerClick,
                            onRecordAudioClick = onRecordAudioClick,
                            onUploadAudioClick = onUploadAudioClick,
                            onUploadImageClick = onUploadImageClick,
                            sharedText = sharedText,
                            onSharedTextConsumed = onConsumeSharedText
                        )
                    }

                    BottomTab.Settings -> {
                        SettingsScreen(innerPadding = innerPadding)
                    }
                }
            }
        }
    }
}


@Composable
fun SafeNestHomeScreen(
    innerPadding: PaddingValues,
    onBlocklistClick: () -> Unit,
    onWhitelistClick: () -> Unit,
    onManageProtectionClick: () -> Unit,

    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize().padding(innerPadding)
            .padding(start = DSSpacing.s6, end = DSSpacing.s6, top = DSSpacing.s9),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        item {
            Text(
                text = "SafeNest Security",
                style = DSTypography.h2.bold,
                color = DSColors.textActionActive
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Security Services",
                style = DSTypography.caption1.regular,
                color = DSColors.textBody,
                modifier = Modifier.padding(top = DSSpacing.s6, bottom = DSSpacing.s2)
            )
        }

        item {
            CallProtectionCard(
                onBlocklistClick = onBlocklistClick,
                onWhitelistClick = onWhitelistClick,
                onManageProtectionClick = onManageProtectionClick
            )
        }

        item {
            FeatureCard(
                icon = ImageVector.vectorResource(id = R.drawable.ic_safe_browsing),
                title = "Safe Browsing",
                description = "Prevent malicious websites from tracking your data or installing unwanted malware.",
                isToggled = true
            ) {

            }
        }

        item {
            FeatureCard(
                icon = ImageVector.vectorResource(id = R.drawable.ic_communication_protection_alt),
                title = "Communication Protection",
                description = "Prevent spam and malicious links in SMS, Messenger, Zalo, and more.",
                isToggled = true
            ) {

            }
        }

        item {
            FeatureCard(
                icon = ImageVector.vectorResource(id = R.drawable.ic_sms_filtering),
                title = "SMS Filtering",
                description = "Prevent spam and malicious links in SMS, Messenger, Zalo, and more.",
                isToggled = true
            ) {

            }
        }

        item {
            ActionFeatureCard(
                icon = ImageVector.vectorResource(id = R.drawable.ic_telegram_bot), // Hoặc R.drawable.ic_chat_smile nếu bạn có file SVG riêng
                title = "SafeNest\nTelegram Bot", // Ký tự \n giúp ngắt thành 2 dòng như thiết kế
                actionText = "Open Telegram",
                description = "Anti-scam assistant: protects chats, links, images and audios.",
                onClick = {
                }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    isToggled: Boolean,
    onClick: () -> Unit
) {
    var checked by remember { mutableStateOf(isToggled) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Cho phép click toàn bộ card
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(DSColors.surfaceActive, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DSColors.iconAction
                    )
                }

                Spacer(modifier = Modifier.width(DSSpacing.s2))

                Text(
                    text = title,
                    style = DSTypography.body2.bold,
                    color = DSColors.textBody,
                    modifier = Modifier.weight(1f)
                )
                DsToggle(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }

            Spacer(modifier = Modifier.height(DSSpacing.s3))

            Text(
                text = description,
                style = DSTypography.caption1.regular,
                color = DSColors.textBody
            )
        }
    }
}


@Composable
fun ActionFeatureCard(
    icon: ImageVector,
    title: String,
    actionText: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Cho phép click toàn bộ card
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(DSColors.surfaceActive, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DSColors.iconAction
                    )
                }

                Spacer(modifier = Modifier.width(DSSpacing.s2))

                Text(
                    text = title,
                    style = DSTypography.body2.bold,
                    color = DSColors.textBody,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = actionText,
                        style = DSTypography.caption2.regular,
                        color = DSColors.textBody
                    )
                    Spacer(modifier = Modifier.width(DSSpacing.s1))
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Open",
                        tint = DSColors.iconBody,
                        modifier = Modifier.size(DSSpacing.s4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(DSSpacing.s3))

            Text(
                text = description,
                style = DSTypography.caption1.regular,
                color = DSColors.textBody
            )
        }
    }
}

@Composable
fun CallProtectionCard(
    onBlocklistClick: () -> Unit,
    onWhitelistClick: () -> Unit,
    onManageProtectionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable {
            onManageProtectionClick()
        }
    ) {
        Column(modifier = Modifier.padding(DSSpacing.s5)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(DSColors.surfaceActive, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_communication_protection),
                            contentDescription = null,
                            tint = DSColors.iconAction
                        )
                    }
                    Spacer(modifier = Modifier.width(DSSpacing.s2))
                    Text(
                        "Call Protection",
                        style = DSTypography.body2.bold,
                        color = DSColors.textBody
                    )
                }
                Icon(
                    ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Details",
                    tint = DSColors.iconBody
                )
            }

            Spacer(modifier = Modifier.height(DSSpacing.s3))
            Text(
                "Blocking 5,240+ known spam numbers",
                style = DSTypography.caption1.regular,
                color = DSColors.textBody
            )

            Spacer(modifier = Modifier.height(DSSpacing.s3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DSSpacing.s3)
            ) {
                StatusChip(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_blocking),
                    iconTint = DSColors.iconError,
                    text = "Blocklist",
                    modifier = Modifier.clickable {
                        onBlocklistClick()
                    }
                )
                StatusChip(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_whitelist),
                    iconTint = DSColors.iconSuccess,
                    text = "Whitelist",
                    modifier = Modifier.clickable {
                        onWhitelistClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(DSSpacing.s2))
            DSButton(
                text = "Manage Protection",
                onClick = {
                    onManageProtectionClick()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = DSSpacing.s2),
                textStyle = DSTypography.caption1.bold
            )
        }
    }
}

@Composable
fun StatusChip(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(DSColors.surface2, RoundedCornerShape(50)) // Bo tròn góc 50%
            .padding(horizontal = DSSpacing.s4, vertical = DSSpacing.s3)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(DSSpacing.s2))
        Text(
            text = text,
            style = DSTypography.caption1.medium,
            color = DSColors.textHeading
        )
    }
}

@Composable
fun SafeNestBottomNavigation(
    onHomeClick: () -> Unit,
    onToolsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    bottomTab: BottomTab
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DSColors.surface1,
        shadowElevation = 16.dp,
    ) {
        Row(
            modifier = Modifier.navigationBarsPadding()
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(
                modifier = Modifier.weight(0.5f)
            )

            CustomTabItem(
                icon = ImageVector.vectorResource(
                    id = if (bottomTab == BottomTab.Home) R.drawable.ic_home_selected
                    else R.drawable.ic_home_unselected
                ),
                label = "Home",
                isSelected = bottomTab == BottomTab.Home,
                onClick = {
                    onHomeClick()
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(
                modifier = Modifier.weight(0.2f)
            )

            CustomTabItem(
                icon = ImageVector.vectorResource(
                    id = if (bottomTab == BottomTab.Tools) R.drawable.ic_tools_selected
                    else R.drawable.ic_tools_unselected
                ),
                label = "Tools",
                isSelected = bottomTab == BottomTab.Tools,
                onClick = {
                    onToolsClick()
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(
                modifier = Modifier.weight(0.2f)
            )

            CustomTabItem(
                icon = ImageVector.vectorResource(
                    id = if (bottomTab == BottomTab.Settings) R.drawable.ic_settings_selected
                    else R.drawable.ic_settings_unselected
                ),
                label = "Settings",
                isSelected = bottomTab == BottomTab.Settings,
                onClick = {
                    onSettingsClick()
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}

@Composable
fun CustomTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) DSColors.textActionActive else DSColors.textNeutral
    val indicatorColor = if (isSelected) DSColors.textActionActive else Color.Transparent

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(3.dp)
                .background(indicatorColor)
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(DSSpacing.s1))

        Text(
            text = label,
            style = DSTypography.caption2.semiBold,
            color = contentColor
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

enum class BottomTab { Home, Tools, Settings }