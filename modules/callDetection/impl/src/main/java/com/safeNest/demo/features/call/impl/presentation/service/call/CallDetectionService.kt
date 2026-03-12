package com.safeNest.demo.features.call.impl.presentation.service.call

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.safeNest.demo.features.call.impl.presentation.service.handler.CallDetectionHandler
import com.safeNest.demo.features.call.impl.presentation.service.handler.CallResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallDetectionService : CallScreeningService() {
    @Inject
    lateinit var callDetectionHandler: CallDetectionHandler

    override fun onScreenCall(details: Call.Details) {

        val incomingNumber = details.handle.schemeSpecificPart
        Log.d("CallDetectionService", "incoming call $incomingNumber")
        CoroutineScope(Dispatchers.IO).launch {

            val allowed = callDetectionHandler.onCallRing(incomingNumber)

            val response = CallResponse.Builder()

            if (allowed is CallResult.Reject) {
                response.setDisallowCall(true)
                    .setRejectCall(true)
            }
            respondToCall(details, response.build())
        }
    }
}