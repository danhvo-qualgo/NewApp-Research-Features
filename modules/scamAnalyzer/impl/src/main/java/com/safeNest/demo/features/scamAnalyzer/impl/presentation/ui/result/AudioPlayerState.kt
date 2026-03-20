package com.safeNest.demo.features.scamAnalyzer.impl.presentation.ui.result

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class AudioPlayerState(context: Context, uri: Uri) {

    var isPlaying by mutableStateOf(false)
        private set
    
    var currentPosition by mutableIntStateOf(0)
        private set
    
    var duration by mutableIntStateOf(0)
        private set
    
    var progress by mutableFloatStateOf(0f)
        private set

    private val player = MediaPlayer().apply {
        setDataSource(context, uri)
        prepare()
        setOnCompletionListener {
            this@AudioPlayerState.isPlaying = false
            this@AudioPlayerState.currentPosition = 0
            this@AudioPlayerState.progress = 0f
        }
    }
    
    init {
        duration = player.duration
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
            isPlaying = false
        } else {
            player.start()
            isPlaying = true
        }
    }
    
    fun updateProgress() {
        if (isPlaying && player.duration > 0) {
            currentPosition = player.currentPosition
            progress = currentPosition.toFloat() / player.duration.toFloat()
        }
    }
    
    fun seekTo(position: Float) {
        val seekPosition = (position * player.duration).toInt()
        player.seekTo(seekPosition)
        currentPosition = seekPosition
        progress = position
    }

    fun release() = player.release()
}

@Composable
fun rememberAudioPlayer(uri: Uri): AudioPlayerState {
    val context = LocalContext.current
    val state = remember(uri) { AudioPlayerState(context, uri) }
    
    LaunchedEffect(state.isPlaying) {
        while (isActive && state.isPlaying) {
            state.updateProgress()
            delay(100) // Update every 100ms
        }
    }
    
    DisposableEffect(uri) { onDispose { state.release() } }
    return state
}

fun formatTime(milliseconds: Int): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
