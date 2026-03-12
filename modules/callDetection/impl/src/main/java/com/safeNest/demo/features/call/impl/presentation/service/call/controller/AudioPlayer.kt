package com.safeNest.demo.features.call.impl.presentation.service.call.controller

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.safeNest.demo.features.call.impl.R

object AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun play(
        context: Context,
        @RawRes resId: Int = R.raw.auto_reply,
        onComplete: () -> Unit
    ) {

        stop()

        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // QUAN TRỌNG
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false

        val mp = MediaPlayer()
        mediaPlayer = mp

        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )

        val afd = context.resources.openRawResourceFd(resId)
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()

        mp.setOnCompletionListener {
            stop()
            onComplete()
        }

        mp.setOnErrorListener { player, _, _ ->
            player.release()
            mediaPlayer = null
            onComplete()
            true
        }

        mp.prepare()
        mp.start()
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }
}