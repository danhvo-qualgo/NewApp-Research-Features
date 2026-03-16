package com.safeNest.demo.features.home.impl.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safeNest.demo.features.home.impl.R

// Màu sắc chủ đạo (Giả định dựa trên thiết kế)
val PrimaryPurple = Color(0xFF4F46E5)
val BackgroundLight = Color(0xFFF0F4FF)
val CardBackground = Color.White
val TextPrimary = Color(0xFF1F2937)
val TextSecondary = Color(0xFF6B7280)
val ColorError = Color(0xFFEF4444)
val ColorSuccess = Color(0xFF10B981)
val ChipBackgroundColor = Color(0xFFF9FAFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onHomeClick: () -> Unit,
    onToolsClick: () -> Unit,
    onManageProtectionClick: () -> Unit,
) {
    val navController = rememberNavController()
    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = { SafeNestBottomNavigation(
            onHomeClick = onHomeClick,
            onToolsClick = onToolsClick
        ) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                SafeNestHomeScreen(
                    onManageProtectionClick = onManageProtectionClick
                )
            }

            composable(BottomNavItem.Tools.route) {
//                ScamAnalyzerScreen() // Composable của màn hình Tools
            }
        }
    }
}

@Composable
fun SafeNestHomeScreen(
    onManageProtectionClick: () -> Unit,

) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SafeNest Security",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
            Text(
                text = "Security Services",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }

        // Card 1: Call Protection (Card đặc biệt có UI phức tạp hơn một chút)
        item {
            CallProtectionCard(
                onManageProtectionClick = onManageProtectionClick
            )
        }

        // Các Card tính năng chuẩn
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
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        .size(44.dp)
                        .background(BackgroundLight, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryPurple
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp
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
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        .size(44.dp)
                        .background(BackgroundLight, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )

                // Cụm "Open Telegram ->"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = actionText,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun CallProtectionCard(
    onManageProtectionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(BackgroundLight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_communication_protection), contentDescription = null, tint = PrimaryPurple)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Call Protection", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Details", tint = TextSecondary)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Blocking 5,240+ known spam numbers", fontSize = 13.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_blocking),
                    iconTint = ColorError,
                    text = "Blocklist"
                )
                StatusChip(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_whitelist),
                    iconTint = ColorSuccess,
                    text = "Whitelist"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onManageProtectionClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Protection", color = Color.White)
            }
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
            .background(ChipBackgroundColor, RoundedCornerShape(50)) // Bo tròn góc 50%
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SafeNestBottomNavigation(
    onHomeClick: () -> Unit,
    onToolsClick: () -> Unit
) {
    // Placeholder cho BottomNavigationBar
    NavigationBar(containerColor = CardBackground) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true,
            onClick = {
                onHomeClick()
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
            label = { Text("Tools") },
            selected = false,
            onClick = {
                onToolsClick()
            }
        )
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Tools : BottomNavItem("tools", "Tools", Icons.Default.Build)
}