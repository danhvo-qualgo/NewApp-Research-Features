package com.safeNest.demo.features.scamAnalyzer.impl.presentation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSRadius
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.scamAnalyzer.impl.R

private val TitlePurple = Color(0xFF4F46E5)

private val SafeGreen = Color(0xFF00A07C)
private val SafeGreenDark = Color(0xFF008365)
private val SafeGreenSurface = Color(0xFFF2FAF8)
private val SafeGreenSurfaceDark = Color(0xFFA3DCCF)
private val ErrorRed = Color(0xFFF22A3D)
private val ErrorSurface = Color(0xFFF0E9F3)
private val ErrorSurfaceDark = Color(0xFFF6DDE5)
private val SurfaceGrayLight = Color(0xFFC2C5CD)
private val SurfaceGray = Color(0xFF5D6070)

data class AnalysisResultUiState(
    val title: String,
    val description: String,
    val icon: Painter,
    val surfacePrimaryColor: Color,
    val surfaceColor: Color,
    val surfaceDarkColor: Color,
)

@Composable
fun AnalysisResultStatus.toUiState(): AnalysisResultUiState {
    return when (this) {
        AnalysisResultStatus.Safe -> AnalysisResultUiState(
            title = "Safe",
            description = "No Risk Alert",
            icon = painterResource(R.drawable.ic_check),
            surfacePrimaryColor = SafeGreen,
            surfaceColor = SafeGreenSurface,
            surfaceDarkColor = SafeGreenSurfaceDark
        )

        AnalysisResultStatus.Scam -> AnalysisResultUiState(
            title = "Scam Detected",
            description = "High Risk Alert",
            icon = painterResource(R.drawable.ic_slash_circle),
            surfacePrimaryColor = ErrorRed,
            surfaceColor = ErrorSurface ,
            surfaceDarkColor = ErrorSurfaceDark
        )

        AnalysisResultStatus.Unverified -> AnalysisResultUiState(
            title = "Unverified",
            description = "n/a",
            icon = painterResource(R.drawable.ic_help_circle),
            surfacePrimaryColor = SurfaceGray,
            surfaceColor = Color.White,
            surfaceDarkColor = SurfaceGrayLight
        )
    }
}


enum class AnalysisResultStatus {
    Safe,
    Scam,
    Unverified,
}

data class AnalysisItem(
    val title: String,
    val description: String
)

sealed class AnalysisResult(
    open val status: AnalysisResultStatus,
    open val analysisItems: List<AnalysisItem>?
) {

    data class Text(
        val originalText: String,
        val maskedText: String,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)

    data class Url(
        val url: String,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)

    data class Image(
        val imageUri: Uri,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)

    data class Audio(
        val audioUri: Uri,
        override val status: AnalysisResultStatus,
        override val analysisItems: List<AnalysisItem>?
    ) : AnalysisResult(status, analysisItems)
}

@Composable
fun AnalysisResultScreen(
    result: AnalysisResult,
    onBackClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = gradientBackground
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopNavBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DSSpacing.s6, vertical = DSSpacing.s6),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.s6)
            ) {
                ResultSummaryCard(result.status)

                AnalysisResultsSection(
                    result
                )
            }
        }
    }
}

@Composable
private fun TopNavBar(onBackClick: () -> Unit) {
    Surface(
        color = DSColors.surfacePrimary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = DSSpacing.s6, vertical = DSSpacing.s4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.s4)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DSColors.surfacePrimary)
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = TitlePurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "Analysis Result",
                style = DSTypography.h4.bold,
                color = TitlePurple,
            )
        }
    }
}

@Composable
private fun ResultSummaryCard(status: AnalysisResultStatus = AnalysisResultStatus.Unverified) {
    val uiState = status.toUiState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DSSpacing.s6)
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(uiState.surfaceColor)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(uiState.surfaceDarkColor)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(uiState.surfacePrimaryColor)
                ) {
                    Icon(
                        painter = uiState.icon,
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }

        Text(
            text = uiState.title,
            style = DSTypography.h4.bold,
            color = uiState.surfacePrimaryColor,
        )

        Text(
            text = uiState.description,
            style = DSTypography.caption1.bold,
            color = uiState.surfacePrimaryColor,
        )
    }
}

@Composable
private fun AnalysisResultsSection(
    analysisResult: AnalysisResult
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s4)
    ) {
        Text(
            text = "Analysis Results",
            style = DSTypography.caption1.semiBold,
            color = DSColors.textBody,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.s2)
        ) {
            when (analysisResult) {
                is AnalysisResult.Text -> {
                    TextContainer(text = analysisResult.originalText)
                    TextContainer(text = analysisResult.maskedText)
                }

                is AnalysisResult.Url -> {
                    TextContainer(text = analysisResult.url)
                }

                is AnalysisResult.Image -> {
                    ImageContainer(uri = analysisResult.imageUri)
                }

                is AnalysisResult.Audio -> {
                    // TODO: implement audio player
                }
            }

        }
    }
}

@Composable
private fun TextContainer(
    text: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DSRadius.xLarge),
        colors = CardDefaults.cardColors(containerColor = DSColors.surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSSpacing.s6),
            style = DSTypography.body2.medium,
            color = DSColors.textBody,
        )
    }
}

@Composable
private fun ImageContainer(
    uri: Uri,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DSRadius.xLarge),
        colors = CardDefaults.cardColors(containerColor = DSColors.surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}


@Preview(showBackground = true, showSystemUi = true, name = "Analysis Result – Safe")
@Composable
private fun AnalysisResultScreenPreview() {
//    AnalysisResultScreen()
}
