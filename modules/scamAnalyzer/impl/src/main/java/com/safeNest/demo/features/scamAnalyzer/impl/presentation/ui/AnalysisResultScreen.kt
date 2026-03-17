package com.safeNest.demo.features.scamAnalyzer.impl.presentation.ui

import android.net.Uri
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSRadius
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisItem
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResultStatus
import com.safeNest.demo.features.scamAnalyzer.impl.R


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
            surfacePrimaryColor = DSColors.surfaceSuccess,
            surfaceColor = DSColors.surfaceSuccessLightest,
            surfaceDarkColor = DSColors.surfaceSuccessLighter
        )

        AnalysisResultStatus.Scam -> AnalysisResultUiState(
            title = "Scam Detected",
            description = "High Risk Alert",
            icon = painterResource(R.drawable.ic_slash_circle),
            surfacePrimaryColor = DSColors.surfaceError,
            surfaceColor = DSColors.surfaceScam,
            surfaceDarkColor = DSColors.surfaceScamDark
        )

        AnalysisResultStatus.Unverified -> AnalysisResultUiState(
            title = "Unverified",
            description = "n/a",
            icon = painterResource(R.drawable.ic_help_circle),
            surfacePrimaryColor = DSColors.surfaceNeutral,
            surfaceColor = DSColors.surfacePrimary,
            surfaceDarkColor = DSColors.surfaceGray
        )
    }
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

                AnalysisResultsSection(result)

                result.analysisItems?.takeIf { it.isNotEmpty() }?.let { items ->
                    AnalysisItemsSection(items)
                }
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
                        tint = DSColors.textAction,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "Analysis Result",
                style = DSTypography.h4.bold,
                color = DSColors.textAction,
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
                        tint = DSColors.iconInverted
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
                    AudioPlayerComponent(uri = analysisResult.audioUri)
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


// ── Analysis Items Section ─────────────────────────────────────────────────────

@Composable
private fun AnalysisItemsSection(items: List<AnalysisItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s4),
    ) {
        Text(
            text = "Why it is suspicious",
            style = DSTypography.caption1.semiBold,
            color = DSColors.textBody,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DSRadius.xxLarge),
            colors = CardDefaults.cardColors(containerColor = DSColors.surfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DSSpacing.s6),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.s6),
            ) {
                items.forEachIndexed { index, item ->
                    AnalysisItemRow(item = item)
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            color = DSColors.surfaceDivider,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisItemRow(item: AnalysisItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DSSpacing.s4),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DSColors.surfaceError),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_circle),
                contentDescription = null,
                tint = DSColors.iconInverted,
                modifier = Modifier.size(14.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.s1),
        ) {
            Text(
                text = item.title,
                style = DSTypography.body2.semiBold,
                color = DSColors.textHeadingDarkest,
            )
            Text(
                text = item.description,
                style = DSTypography.caption1.regular,
                color = DSColors.textBodyMuted,
            )
        }
    }
}

@Preview(showBackground = true, name = "Analysis Items Section")
@Composable
private fun AnalysisItemsSectionPreview() {
    AnalysisItemsSection(
        items = listOf(
            AnalysisItem(
                title = "Artificial Urgency",
                description = "The message uses high-pressure language (\"URGENT\", \"immediately\") to force a quick reaction.",
            ),
            AnalysisItem(
                title = "Suspicious Link",
                description = "The URL does not match official bank domains and uses masking techniques.",
            ),
            AnalysisItem(
                title = "Unverified Sender",
                description = "Phrases like \"Action Required Immediately\" and \"Account Suspension\" are typical pressure tactics.",
            ),
        )
    )
}

private val WaveformBars = listOf(
    0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.3f, 0.6f,
    0.8f, 0.5f, 0.4f, 0.9f, 0.6f, 0.3f, 0.7f, 0.5f, 0.8f, 0.4f,
    0.6f, 0.3f, 0.9f, 0.5f, 0.7f, 0.4f, 0.6f, 0.8f, 0.3f, 0.5f,
)

@Composable
fun AudioPlayerComponent(
    uri: Uri,
    modifier: Modifier = Modifier,
) {
    val player = rememberAudioPlayer(uri)

    AudioPlayerComponentContent(
        isPlaying = player.isPlaying,
        onPlayPauseClick = player::togglePlayPause,
        modifier = modifier,
    )
}

@Composable
private fun AudioPlayerComponentContent(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DSRadius.xLarge),
        colors = CardDefaults.cardColors(containerColor = DSColors.surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSSpacing.s6),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.s4),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DSColors.surfaceError),
            ) {
                IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_play
                        ),
                        contentDescription = "",
                        tint = DSColors.iconInverted,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            FakeWaveform(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
            )
        }
    }
}

@Composable
private fun FakeWaveform(
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    barGap: Dp = 3.dp,
    barColor: Color = DSColors.waveformBar,
) {
    Canvas(modifier = modifier) {
        val barPx = barWidth.toPx()
        val stepPx = barPx + barGap.toPx()
        val count = (size.width / stepPx).toInt().coerceAtMost(WaveformBars.size)

        for (i in 0 until count) {
            val barHeight = size.height * WaveformBars[i % WaveformBars.size]
            val x = i * stepPx + barPx / 2f
            drawLine(
                color = barColor,
                start = Offset(x, (size.height - barHeight) / 2f),
                end = Offset(x, (size.height + barHeight) / 2f),
                strokeWidth = barPx,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview(showBackground = true, name = "Audio Player – idle")
@Composable
private fun AudioPlayerComponentPreview() {
    AudioPlayerComponentContent(
        isPlaying = false,
        onPlayPauseClick = {},
    )
}

