package com.safeNest.demo.features.home.impl.presentation.ui.mediaPreview

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.home.impl.R

enum class MediaType {
    AUDIO, IMAGE
}

@Composable
fun MediaPreviewScreen(
    mediaUri: Uri,
    mediaType: MediaType,
    onAnalyzeClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    mediaPreviewViewModel: MediaPreviewViewModel = hiltViewModel(),
    autoAnalyze: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val uiState by mediaPreviewViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mediaUri) {
        mediaPreviewViewModel.loadMediaInfo(mediaUri)
    }

    LaunchedEffect(autoAnalyze) {
        if (autoAnalyze && !uiState.isAnalyzing) {
            when (mediaType) {
                MediaType.AUDIO -> mediaPreviewViewModel.analyzeAudio(mediaUri)
                MediaType.IMAGE -> mediaPreviewViewModel.analyzeImage(mediaUri)
            }
        }
    }

    LaunchedEffect(Unit) {
        mediaPreviewViewModel.events.collect { event ->
            when (event) {
                is MediaPreviewEvent.AnalysisSuccess -> {
                    onAnalyzeClick(event.resultKey)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopNavBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = DSSpacing.s6),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(DSSpacing.s6))

                Text(
                    text = "Identify scams in messages, links, or images using our AI-powered analyzer.",
                    style = DSTypography.body2.medium,
                    color = DSColors.textHeading,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                when (mediaType) {
                    MediaType.AUDIO -> {
                        MediaFileCard(
                            fileName = uiState.fileName,
                            fileSize = uiState.fileSize,
                            iconRes = R.drawable.ic_record_audio,
                            onDeleteClick = onDeleteClick
                        )
                    }

                    MediaType.IMAGE -> {
                        ImagePreviewCard(
                            imageUri = mediaUri,
                            fileName = uiState.fileName,
                            fileSize = uiState.fileSize,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                DSButton(
                    text = "Analyze Now",
                    onClick = {
                        when (mediaType) {
                            MediaType.AUDIO -> mediaPreviewViewModel.analyzeAudio(mediaUri)
                            MediaType.IMAGE -> mediaPreviewViewModel.analyzeImage(mediaUri)
                        }
                    },
                    enabled = !uiState.isAnalyzing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    textStyle = DSTypography.body2.bold,
                )
            }
        }

        if (uiState.isAnalyzing) {
            FullScreenAnalyzing()
        }

        uiState.errorMessage?.let { error ->
            ErrorDialog(
                message = error,
                onDismiss = { mediaPreviewViewModel.clearError() }
            )
        }
    }
}

@Composable
private fun MediaFileCard(
    fileName: String,
    fileSize: String,
    iconRes: Int,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(DSColors.surfaceActive, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = fileName,
                    style = DSTypography.body2.bold,
                    color = DSColors.textBody,
                    maxLines = 1
                )
                Text(
                    text = fileSize,
                    style = DSTypography.caption2.regular,
                    color = DSColors.textNeutral
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_trash),
                    contentDescription = "Delete",
                    tint = DSColors.iconBody,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(16.dp),
            color = DSColors.surfacePrimary,
            modifier = Modifier.padding(DSSpacing.s6)
        ) {
            Column(
                modifier = Modifier.padding(DSSpacing.s6),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSSpacing.s4)
            ) {
                Text(
                    text = "Error",
                    style = DSTypography.h4.bold,
                    color = DSColors.textError
                )
                Text(
                    text = message,
                    style = DSTypography.body2.medium,
                    color = DSColors.textBody,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                DSButton(
                    text = "OK",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = DSTypography.body2.bold
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewCard(
    imageUri: Uri,
    fileName: String,
    fileSize: String,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun FullScreenAnalyzing() {
    Dialog(
        onDismissRequest = { },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DSColors.surfacePrimary,
                modifier = Modifier.padding(DSSpacing.s6)
            ) {
                Column(
                    modifier = Modifier.padding(DSSpacing.s8),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DSSpacing.s4)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = DSColors.textActionActive,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "Analyzing...",
                        style = DSTypography.body1.semiBold,
                        color = DSColors.textHeading
                    )
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
                text = "Scam Analyzer",
                style = DSTypography.h4.bold,
                color = DSColors.textAction,
            )
        }
    }
}
