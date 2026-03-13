package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors from the design JSON
private val PermGradientStart = Color(0xFFD5D9F9)
private val PermTitlePurple = Color(0xFF4338CA)
private val PermSubtitlePurple = Color(0xFF4F46E5).copy(alpha = 0.85f)
private val PermTextDark = Color(0xFF1C1D22)
private val PermTextGray = Color(0xFF848899)
private val PermIconBg = Color(0xFFEEF0FF)
private val PermCardBg = Color.White
private val PermShieldBg = Color.White
private val PermPrimaryIndigo = Color(0xFF4F46E5)
private val PermButtonDisabled = Color(0xFFC7D2F6)

data class PermissionItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

@Composable
fun PermissionsRequiredScreen(
    onStartClick: () -> Unit = {},
) {
    val permissions = remember {
        listOf(
            PermissionItem(
                icon = Icons.Outlined.Accessibility,
                title = "Accessibility",
                subtitle = "Detects and blocks fraudulent\nlinks in apps and browsers.",
            ),
            PermissionItem(
                icon = Icons.Outlined.Notifications,
                title = "Notification Listener",
                subtitle = "Scans incoming SMS and chat\nalerts for phishing attempts.",
            ),
            PermissionItem(
                icon = Icons.Outlined.Layers,
                title = "Display over apps",
                subtitle = "Allows showing security warnings on top",
            ),
            PermissionItem(
                icon = Icons.Outlined.Phone,
                title = "Phone & Contacts",
                subtitle = "Identify and block scam calls\nfrom unknown numbers.",
            ),
            PermissionItem(
                icon = Icons.Outlined.Mic,
                title = "Microphone",
                subtitle = "Record audio for scam call detection.",
            ),
            PermissionItem(
                icon = Icons.Outlined.Dns,
                title = "Private DNS",
                subtitle = "Route DNS queries through safe servers.",
            ),
            PermissionItem(
                icon = Icons.Outlined.Storage,
                title = "DNS Proxy",
                subtitle = "Filter malicious domains via proxy.",
            ),
            PermissionItem(
                icon = Icons.Outlined.Message,
                title = "SMS Filtering",
                subtitle = "Block scam SMS before they reach you.",
            ),
        )
    }

    val toggleStates = remember { mutableStateListOf(*Array(permissions.size) { false }) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(PermGradientStart, Color(0xFFFAFAFA)),
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                PermissionsHeader()
                PermissionsCardList(
                    permissions = permissions,
                    toggleStates = toggleStates,
                    onToggle = { index, checked -> toggleStates[index] = checked },
                )
            }

            // Bottom dock bar with Start button
            PermissionsDockBar(onStartClick = onStartClick)
        }
    }
}

@Composable
private fun PermissionsHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Shield icon circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(color = PermShieldBg, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = PermPrimaryIndigo,
                modifier = Modifier.size(32.dp)
            )
        }

        // Title + subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Permissions Required",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = PermTitlePurple,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            Text(
                text = "To provide real-time protection against\nscams, please enable the following.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = PermSubtitlePurple,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun PermissionsCardList(
    permissions: List<PermissionItem>,
    toggleStates: List<Boolean>,
    onToggle: (Int, Boolean) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        permissions.forEachIndexed { index, item ->
            PermissionCard(
                item = item,
                checked = toggleStates[index],
                onCheckedChange = { onToggle(index, it) }
            )
        }
    }
}

@Composable
private fun PermissionCard(
    item: PermissionItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PermCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = PermIconBg, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = PermPrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title + subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PermTextDark,
                    lineHeight = 24.sp
                )
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = PermTextGray,
                    lineHeight = 16.sp
                )
            }

            // Toggle
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PermPrimaryIndigo,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Black.copy(alpha = 0.1f),
                    uncheckedBorderColor = Color.Transparent,
                )
            )
        }
    }
}

@Composable
private fun PermissionsDockBar(onStartClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF9F9F9).copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PermButtonDisabled,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Start",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PermissionsRequiredScreenPreview() {
    Surface {
        PermissionsRequiredScreen()
    }
}
