package com.safeNest.demo.features.home.impl.presentation.ui.recording

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.home.impl.R
import kotlinx.coroutines.delay

val RecordingRed = Color(0xFFF22A3D)

@Composable
fun RecordingScreen(
    onStopRecording: () -> Unit,
    onAnalysisSuccess: (Uri) -> Unit,
    recordingViewModel: RecordingViewModel = hiltViewModel()
) {
    var elapsedTime by remember { mutableStateOf(0L) }
    val uiState by recordingViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        recordingViewModel.events.collect { event ->
            when (event) {
                is RecordingEvent.RecordingStopped -> {
                    event.audioUri?.let { uri ->
                        onAnalysisSuccess(uri)
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.isRecording) {
        if (uiState.isRecording) {
            elapsedTime = 0L
            while (true) {
                delay(1000)
                elapsedTime++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomActionBar(
                    isRecording = uiState.isRecording,
                    isAnalyzing = false,
                    onStartRecording = {
                        recordingViewModel.startRecording()
                    },
                    onStopRecording = {
                        recordingViewModel.stopRecording()
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 64.dp, start = DSSpacing.s6, end = DSSpacing.s6),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isRecording) "Recording...." else "Record Audio",
                    style = DSTypography.h2.bold,
                    color = DSColors.textActionActive,
                    lineHeight = 42.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(56.dp))

                RecordingVisualization(isRecording = uiState.isRecording)

                Spacer(modifier = Modifier.height(24.dp))

                TimerDisplay(elapsedTime = elapsedTime)

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isRecording) {
                    RecordingWaveIndicator()
                }
            }
        }
    }
}

@Composable
private fun RecordingVisualization(isRecording: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(272.dp)
    ) {
        if (isRecording) {
            Surface(
                modifier = Modifier
                    .size(272.dp)
                    .scale(scale),
                shape = CircleShape,
                color = RecordingRed.copy(alpha = 0.1f)
            ) {}
        }

        Surface(
            modifier = Modifier.size(224.dp),
            shape = CircleShape,
            color = if (isRecording) RecordingRed.copy(alpha = 0.4f) else DSColors.surface1.copy(alpha = 0.3f)
        ) {}

        Surface(
            modifier = Modifier.size(176.dp),
            shape = CircleShape,
            color = if (isRecording) RecordingRed else DSColors.surfaceActive
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_microphone),
                    contentDescription = if (isRecording) "Recording" else "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

@Composable
private fun TimerDisplay(elapsedTime: Long) {
    val hours = elapsedTime / 3600
    val minutes = (elapsedTime % 3600) / 60
    val seconds = elapsedTime % 60

    Text(
        text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
        style = DSTypography.body2.bold,
        color = DSColors.textBody,
        fontSize = 16.sp
    )
}

@Composable
private fun RecordingWaveIndicator() {
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

@Composable
private fun BottomActionBar(
    isRecording: Boolean,
    isAnalyzing: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DSColors.surface1.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DSSpacing.s6, vertical = DSSpacing.s4),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DSButton(
                text = if (isRecording) "Stop Recording" else "Start Recording",
                onClick = if (isRecording) onStopRecording else onStartRecording,
                enabled = !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                textStyle = DSTypography.body2.bold,
            )
        }
    }
}
