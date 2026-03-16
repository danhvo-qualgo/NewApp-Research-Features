package com.safeNest.demo.features.scamAnalyzer.impl.presentation

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

class AudioPlayerState(context: Context, uri: Uri) {

    var isPlaying by mutableStateOf(false)
        private set

    private val player = MediaPlayer().apply {
        setDataSource(context, uri)
        prepare()
        setOnCompletionListener { this@AudioPlayerState.isPlaying = false }
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

    fun release() = player.release()
}

@Composable
fun rememberAudioPlayer(uri: Uri): AudioPlayerState {
    val context = LocalContext.current
    val state = remember(uri) { AudioPlayerState(context, uri) }
    DisposableEffect(uri) { onDispose { state.release() } }
    return state
}
