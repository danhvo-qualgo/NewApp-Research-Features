package com.safeNest.demo.call.main.impl.presentation.service.call.controller

import android.content.Context
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import com.safeNest.demo.call.main.impl.presentation.service.recorder.AudioRouter

object AutoAnswerController {

    fun start(context: Context, call: Call) {

        Log.d("AutoAnswerController", "AutoAnswerController start")
        call.answer(VideoProfile.STATE_AUDIO_ONLY)

        val callback = object : Call.Callback() {

            override fun onStateChanged(call: Call, state: Int) {

                Log.d("AutoAnswerController", "onStateChanged $state")
                when (state) {

                    Call.STATE_ACTIVE -> {

                        call.unregisterCallback(this)

                        // ❌ Không bật loa ngoài nữa
                        AudioRouter.toSpeaker(context)

//                        RecorderService.start(context)
                        AudioPlayer.play(context) {
                        }
                    }

                    Call.STATE_DISCONNECTED -> {
                        call.unregisterCallback(this)
                    }
                }
            }
        }

        call.registerCallback(callback)
    }
}