package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Extracting Colors from the JSON
val GradientStart = Color(0xFFD5D9F9)
val TitlePurple = Color(0xFF4338CA)
val TextDark = Color(0xFF1C1D22)
val TextGray1 = Color(0xFF84899A)
val SurfaceLightGray = Color(0xFFF9F9F9)
val PrimaryIndigo = Color(0xFF4F46E5)
val MediaTextColor = Color(0xFF454955)

// Additional colors from the Result screen JSON
val SafeGreen = Color(0xFF00A07C)
val SafeGreenDark = Color(0xFF008365)
val SafeGreenLight = Color(0xFFF2FAF8)
val SuccessBorder = Color(0xFFA2DCCF)

@Composable
fun AnalysisResultScreen(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GradientStart, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ResultTopBar(onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                ResultOverviewCard()
                AnalysisResultsList()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ResultTopBar(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = "Analysis Result",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TitlePurple
            )
        }
    }
}

@Composable
private fun ResultOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(SafeGreenLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SafeGreen, CircleShape)
                        .border(1.dp, SuccessBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Safe",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreen
                )
                Text(
                    text = "No Risk Alert",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreenDark
                )
            }
        }
    }
}

@Composable
private fun AnalysisResultsList() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Analysis Results",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark.copy(alpha = 0.7f)
        )

        // Original message with highlights
        AnalysisResultItem(isMasked = false)

        // Masked version
        AnalysisResultItem(
            text = "Dear N***** A, your account 01234**** has been locked. Please call \n+84 90***** to verify your identity immediately.",
            isMasked = true
        )
    }
}

@Composable
private fun AnalysisResultItem(
    text: String = "",
    isMasked: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            if (!isMasked) {
                val annotatedString = buildAnnotatedString {
                    append("Dear ")
                    withStyle(style = SpanStyle(background = SafeGreenLight)) {
                        append("Nguyen Van A")
                    }
                    append(" your account ")
                    withStyle(style = SpanStyle(background = SafeGreenLight)) {
                        append("018726547")
                    }
                    append(" has been locked. Please call ")
                    withStyle(style = SpanStyle(background = SafeGreenLight)) {
                        append("+84 908765678")
                    }
                    append(" to verify your identity immediately.")
                }
                Text(
                    text = annotatedString,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = TextDark
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = TextDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    text: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val color = if (isActive) PrimaryIndigo else TextGray1

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Preview
@Composable
fun AnalysisResultPreview() {
    Surface {
        AnalysisResultScreen(onBackClick = {})
    }
}
