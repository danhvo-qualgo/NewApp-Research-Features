package com.safeNest.demo.features.callProtection.impl.presentation.service.call

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import com.safeNest.demo.features.callProtection.impl.presentation.service.handler.CallDetectionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallDetectionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var callDetectionHandler: CallDetectionHandler

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        runnable?.let {
            job?.cancel()
            handler.removeCallbacks(it)
        }

        runnable = Runnable {
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER).orEmpty()

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    lastState = state
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        job = coroutineScope.launch {
                            callDetectionHandler.onCallRing(phoneNumber)
                        }
                    }
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (TelephonyManager.EXTRA_STATE_RINGING == lastState) {
                        callDetectionHandler.onCallAnswer()
                    }
                    lastState = state
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    when (lastState) {
                        TelephonyManager.EXTRA_STATE_RINGING -> {
                        }

                        TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        }

                        else -> {
                        }
                    }
                    lastState = state
                    callDetectionHandler.onCallEnd()
                }
            }
        }.also {
            handler.postDelayed(it, 500L)
        }
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper())
        private var runnable: Runnable? = null
        private val coroutineScope = CoroutineScope(Dispatchers.Main)
        private var job: Job? = null
        private var lastState: String? = TelephonyManager.EXTRA_STATE_IDLE
    }
}