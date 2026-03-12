package com.safeNest.demo.call.main.impl.presentation.service.call

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import com.safeNest.demo.call.main.impl.presentation.service.handler.CallDetectionHandler
import com.safeNest.demo.call.main.impl.presentation.service.handler.CallResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MyConnectionService : ConnectionService() {
    @Inject
    lateinit var callDetectionHandler: CallDetectionHandler

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {

        val incomingNumber =
            request.address?.schemeSpecificPart ?: "unknown"

        Log.d("CallDetectionService", "Incoming: $incomingNumber")

        val connection = MyTelecomConnection().apply {
            setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
            setRinging()
        }

        CoroutineScope(Dispatchers.IO).launch {

            val result = callDetectionHandler.onCallRing(incomingNumber)

            if (result is CallResult.Reject) {

                withContext(Dispatchers.Main) {
                    connection.setDisconnected(
                        DisconnectCause(DisconnectCause.REJECTED)
                    )
                    connection.destroy()
                }
            }
        }

        return connection
    }
}

class MyTelecomConnection : Connection() {

    init {
        setConnectionCapabilities(
            CAPABILITY_SUPPORT_HOLD or
                    CAPABILITY_MUTE
        )
        setAudioModeIsVoip(false)
    }

    override fun onAnswer() {
        setActive()
    }

    override fun onDisconnect() {
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }
}
