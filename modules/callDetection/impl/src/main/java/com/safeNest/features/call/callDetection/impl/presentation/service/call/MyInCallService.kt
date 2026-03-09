package com.safeNest.features.call.callDetection.impl.presentation.service.call

import android.telecom.Call
import android.telecom.InCallService
import com.safeNest.features.call.callDetection.impl.presentation.ui.incall.InCallActivity

class MyInCallService: InCallService() {

    companion object {
        var currentCall: Call? = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        currentCall = call

        if (call.state == Call.STATE_RINGING) {
            InCallActivity.start(this)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        currentCall = null
    }
}