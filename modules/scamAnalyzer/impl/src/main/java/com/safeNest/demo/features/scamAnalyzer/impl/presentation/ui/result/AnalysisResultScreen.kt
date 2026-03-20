package com.safeNest.demo.features.scamAnalyzer.impl.presentation.ui.result

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSRadius
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisItem
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResult
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisResultType
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisStatus
import com.safeNest.demo.features.scamAnalyzer.impl.R


data class AnalysisResultModel(
    val title: String,
    val description: String,
    val icon: Painter,
    val surfacePrimaryColor: Color,
    val surfaceColor: Color,
    val surfaceDarkColor: Color,
)

@Composable
fun AnalysisStatus.toUiModel(): AnalysisResultModel {
    return when (this) {
        AnalysisStatus.Safe -> AnalysisResultModel(
            title = "Safe",
            description = "No Risk Alert",
            icon = painterResource(R.drawable.ic_check),
            surfacePrimaryColor = DSColors.surfaceSuccess,
            surfaceColor = DSColors.surfaceSuccessLightest,
            surfaceDarkColor = DSColors.surfaceSuccessLighter
        )

        AnalysisStatus.Scam -> AnalysisResultModel(
            title = "Scam Detected",
            description = "High Risk Alert",
            icon = painterResource(R.drawable.ic_slash_circle),
            surfacePrimaryColor = DSColors.surfaceError,
            surfaceColor = DSColors.surfaceScam,
            surfaceDarkColor = DSColors.surfaceScamDark
        )

        AnalysisStatus.Unverified -> AnalysisResultModel(
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
    onBackClick: () -> Unit = {},
    viewModel: AnalysisResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = gradientBackground
            )
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DSColors.textAction
                    )
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DSSpacing.s6),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        style = DSTypography.body1.medium,
                        color = DSColors.textError
                    )
                }
            }

            uiState.analysisResult != null -> {
                AnalysisResultContent(
                    result = uiState.analysisResult!!,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun AnalysisResultContent(
    result: AnalysisResult,
    onBackClick: () -> Unit
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

            result.keyFindings?.takeIf { it.isNotEmpty() }?.let { items ->
                AnalysisItemsSection(items)
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
private fun ResultSummaryCard(status: AnalysisStatus = AnalysisStatus.Unverified) {
    val uiState = status.toUiModel()
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
            verticalArrangement = Arrangement.spacedBy(DSSpacing.s4)
        ) {
            when (val data = analysisResult.data) {
                is AnalysisResultType.Text -> {
                    TextResultItem(
                        label = "Original Text",
                        text = data.rawText
                    )
                    TextResultItem(
                        label = "Redacted Text",
                        text = data.redactedMessage
                    )
                }

                is AnalysisResultType.Url -> {
                    TextResultItem(
                        label = "URL",
                        text = data.url
                    )
                }

                is AnalysisResultType.Image -> {
                    ImageResultItem(uri = data.uri.toUri())
                }

                is AnalysisResultType.Audio -> {
                    AudioResultItem(uri = data.uri.toUri())
                }
            }
        }
    }
}

@Composable
private fun TextResultItem(
    label: String,
    text: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        Text(
            text = label,
            style = DSTypography.caption2.semiBold,
            color = DSColors.textNeutral,
        )
        TextContainer(text = text)
    }
}

@Composable
private fun ImageResultItem(uri: Uri) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        ImageContainer(uri = uri)
    }
}

@Composable
private fun AudioResultItem(uri: Uri) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        AudioPlayerComponent(uri = uri)
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

@Composable
fun AudioPlayerComponent(
    uri: Uri,
    modifier: Modifier = Modifier,
) {
    val player = rememberAudioPlayer(uri)

    AudioPlayerComponentContent(
        isPlaying = player.isPlaying,
        currentPosition = player.currentPosition,
        duration = player.duration,
        progress = player.progress,
        onPlayPauseClick = player::togglePlayPause,
        onSeek = player::seekTo,
        modifier = modifier,
    )
}

@Composable
private fun AudioPlayerComponentContent(
    isPlaying: Boolean,
    currentPosition: Int,
    duration: Int,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
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
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = DSColors.iconInverted,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Waveform visualization
                if (isPlaying) {
                    AnimatedWaveIndicator()
                } else {
                    StaticWaveIndicator()
                }

                // Progress bar (custom without thumb)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DSColors.surfaceGrayLightest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(DSColors.surfaceError)
                    )
                }

                // Time labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = DSTypography.caption2.regular,
                        color = DSColors.textBody,
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatTime(duration),
                        style = DSTypography.caption2.regular,
                        color = DSColors.textBody,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedWaveIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy((-8).dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            AnimatedWaveIcon(delay = index * 200)
        }
    }
}

@Composable
private fun StaticWaveIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy((-8).dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_recording_wave),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(0.7f)
                )
            }
        }
    }
}

@Composable
private fun AnimatedWaveIcon(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_recording_wave),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
        )
    }
}

@Preview(showBackground = true, name = "Audio Player – idle")
@Composable
private fun AudioPlayerComponentPreview() {
    AudioPlayerComponentContent(
        isPlaying = false,
        currentPosition = 37000, // 0:37
        duration = 143000, // 2:23
        progress = 0.26f,
        onPlayPauseClick = {},
        onSeek = {},
    )
}

