package com.safeNest.features.call.callDetection.impl.presentation.service.recorder

import android.content.Context
import android.media.AudioManager

object AudioRouter {

    fun toSpeaker(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java)

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
    }
}