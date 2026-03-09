package com.safeNest.features.call.callDetection.impl.presentation.ui.incall

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.telecom.VideoProfile
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.safeNest.features.call.callDetection.impl.presentation.navigator.Screen
import com.safeNest.features.call.callDetection.impl.presentation.service.call.MyInCallService
import com.safeNest.features.call.callDetection.impl.presentation.service.call.controller.AudioPlayer
import com.safeNest.features.call.callDetection.impl.presentation.service.call.controller.AutoAnswerController
import com.safeNest.features.call.callDetection.impl.presentation.service.recorder.RecorderService
import com.safeNest.features.call.callDetection.impl.presentation.ui.home.HomeScreen
import com.safeNest.features.call.callDetection.impl.presentation.ui.home.HomeViewModel
import com.safeNest.features.call.callDetection.impl.presentation.ui.whitelist.WhitelistScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InCallScreen()
        }
    }

    @Composable
    fun InCallScreen() {
        val call = MyInCallService.currentCall
        val context = LocalContext.current
        val activity = context as? Activity
        var isCalling by remember { mutableStateOf(0) }

        // Listen call state
        LaunchedEffect(call) {
            call?.registerCallback(object : Call.Callback() {
                override fun onStateChanged(call: Call, state: Int) {
                    if (state == Call.STATE_DISCONNECTED) {
                        activity?.runOnUiThread {
                            activity.finish()
                        }
                        RecorderService.stop(context)
                        call.unregisterCallback(this)
                    }
                }
            })
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {

            if (isCalling == 0) {
                Button(
                    onClick = {
                        isCalling = 1
                        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text("Reply")
                }

                Button(
                    onClick = {
                        call?.disconnect()
                        activity?.finish()
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = {
                        isCalling = 2
                        call?.let {
//                            it.answer(VideoProfile.STATE_AUDIO_ONLY)
                            AutoAnswerController.start(context, it)
                        }
//                        call?.registerCallback(object : Call.Callback() {
//
//                            override fun onStateChanged(call: Call, state: Int) {
//
//                                if (state == Call.STATE_ACTIVE) {
//
//                                    AudioPlayer.play(context) {
//                                        // Sau khi phát xong thì end call (tuỳ bạn)
////                                        call.disconnect()
////                                        activity?.finish()
//                                        activity?.let { AutoAnswerController.start(it, call) }
//                                    }
//
//                                    // Unregister để tránh gọi nhiều lần
//                                    call.unregisterCallback(this)
//                                }
//                            }
//                        })
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text("Auto Reply")
                }
            } else {
                Button(
                    onClick = {
                        call?.disconnect()
                        activity?.finish()
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text("End")
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, InCallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}