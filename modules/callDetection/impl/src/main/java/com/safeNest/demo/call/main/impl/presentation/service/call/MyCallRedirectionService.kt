package com.safeNest.demo.call.main.impl.presentation.service.call

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import android.util.Log
import android.widget.Toast
import com.safeNest.demo.call.main.impl.presentation.service.handler.CallDetectionHandler
import com.safeNest.demo.call.main.impl.presentation.service.handler.CallResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("NewApi")
@AndroidEntryPoint
class MyCallRedirectionService : CallRedirectionService() {

    @Inject
    lateinit var callDetectionHandler: CallDetectionHandler

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {

        val originalNumber = handle.schemeSpecificPart
        Log.d("RedirectService", "Original: $originalNumber")

        CoroutineScope(Dispatchers.IO).launch {

            val result = callDetectionHandler.onCallRing(originalNumber)

            withContext(Dispatchers.Main) {

                if (result is CallResult.Reject) {
                    Log.d("RedirectService", "Call rejected")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            applicationContext,
                            "Cannot perform this call",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    cancelCall()
                } else {
                    placeCallUnmodified()
                }
            }
        }
    }
}