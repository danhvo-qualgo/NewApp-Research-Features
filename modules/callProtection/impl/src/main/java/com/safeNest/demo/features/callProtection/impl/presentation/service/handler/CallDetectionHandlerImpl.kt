package com.safeNest.demo.features.callProtection.impl.presentation.service.handler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.api.domain.model.GetCallerIdInfoUseCase
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.domain.common.normalizePhoneNumber
import com.safeNest.demo.features.callProtection.impl.domain.usecase.AddCallTrackingUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableBlockListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.EnableWhiteListUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetCallTrackingUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetMasterBlocklistNumberUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetMasterWhitelistNumberUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.GetWhitelistByNumberUseCase
import com.safeNest.demo.features.callProtection.impl.domain.usecase.IsBlocklistPatternsUseCase
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.callProtection.impl.presentation.service.call.CallDetectionPopup
import com.safeNest.demo.features.commonKotlin.IncomingCallData
import com.safeNest.demo.features.commonKotlin.IncomingCallType
import com.safeNest.demo.features.commonKotlin.incomingCallSharedFlow
import com.uney.core.router.RouterManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallDetectionHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getWhitelistByNumberUseCase: GetWhitelistByNumberUseCase,
    private val isBlocklistPatternsUseCase: IsBlocklistPatternsUseCase,
    private val enableBlockListUseCase: EnableBlockListUseCase,
    private val enableWhiteListUseCase: EnableWhiteListUseCase,
    private val getMasterBlocklistNumberUseCase: GetMasterBlocklistNumberUseCase,
    private val getMasterWhitelistNumberUseCase: GetMasterWhitelistNumberUseCase,
    private val getCallerIdUseCase: GetCallerIdInfoUseCase,
    private val addCallTrackingUseCase: AddCallTrackingUseCase,
    private val getCallTrackingUseCase: GetCallTrackingUseCase,
    private val routerManager: RouterManager
) : CallDetectionHandler {

    override suspend fun onCallRing(phoneNumber: String, isIncoming: Boolean): CallResult {
        val normalizePhoneNumber = normalizePhoneNumber(phoneNumber)
        Log.d("CallDetectionHandlerImpl", "normalizePhoneNumber: $normalizePhoneNumber")
        if (getMasterWhitelistNumberUseCase(normalizePhoneNumber).first() != null) {
            return CallResult.Allow()
        }
        if ((getMasterBlocklistNumberUseCase(normalizePhoneNumber).first() != null
            || getMasterBlocklistNumberUseCase(phoneNumber).first() != null) && isIncoming) {
            callEvent(normalizePhoneNumber, "KinShield just block a call from your blocklist.", IncomingCallType.BLOCKLIST)
            return CallResult.Reject
        }
        Log.d("CallDetectionHandlerImpl", "start check whitelist: $normalizePhoneNumber")

        val isEnableWhitelist = enableWhiteListUseCase.isEnable().first()
        val isEnableBlacklist = enableBlockListUseCase.isEnable().first()
        if (isIncoming) addCallTrackingUseCase(normalizePhoneNumber)
        if (isEnableWhitelist) {
            return getWhitelistByNumberUseCase(normalizePhoneNumber).first()?.let {
                CallResult.Allow()
            } ?: run {
                callEvent(normalizePhoneNumber, "Call blocked because it is not in Whitelist.", IncomingCallType.WHITELIST)
                onCallAnswer(normalizePhoneNumber)
                CallResult.Reject
            }
        }
        Log.v("CallDetectionHandlerImpl", "start check blocklist: $normalizePhoneNumber")

        if (isIncoming && isEnableBlacklist && (isBlocklistPatternsUseCase(phoneNumber).first()
                    || isBlocklistPatternsUseCase(normalizePhoneNumber).first())) {
            onCallAnswer(normalizePhoneNumber)
            callEvent(normalizePhoneNumber, "KinShield just block a call from your blocklist.", IncomingCallType.BLOCKLIST)
            return CallResult.Reject
        }

        if (isIncoming)
            getCallerIdUseCase(normalizePhoneNumber)?.let {
                Log.v("CallDetectionHandlerImpl", "onCallRing: $it")
                return when(it.type) {
                    CallerIdInfoType.SCAM -> {

                        Log.v("CallDetectionHandlerImpl", "onCallRing scam: $it")
                        callEvent(normalizePhoneNumber, "KinShield helped block  because it is identified as scam by community. Tap for more.",IncomingCallType.CALLER_ID)
                        onCallAnswer(normalizePhoneNumber)
                        CallResult.Reject
                    }
                    else -> {
                        if (it.type == CallerIdInfoType.SPAM) {
                            callEvent(normalizePhoneNumber, "The caller is identified by community as spam. Tap to see detail.",IncomingCallType.CALLER_ID)
                        }

                        Log.v("CallDetectionHandlerImpl", "onCallRing spam: $it")
                        Handler(Looper.getMainLooper()).post {
                            CallDetectionPopup.show(context, CallDetectionPopup.PopupContent(normalizePhoneNumber, it.label, it.type))
                        }
                        CallResult.Allow()
                    }
                }
            }

        return CallResult.Allow()
    }

    override fun onCallAnswer(phoneNumber: String) {
        CallDetectionPopup.dismiss(context)
    }

    override suspend fun onCallEnd(phoneNumber: String) {
        CallDetectionPopup.dismiss(context)
        val normalizePhoneNumber = normalizePhoneNumber(phoneNumber)
        if (getMasterWhitelistNumberUseCase(normalizePhoneNumber).first() != null)
            return

        if (getMasterBlocklistNumberUseCase(normalizePhoneNumber).first() != null
            || getMasterBlocklistNumberUseCase(phoneNumber).first() != null)
            return
        val count = getCallTrackingUseCase(normalizePhoneNumber)?.callCount ?: 0
        if (count > 1) {
            showSpamBlockedNotification(context, normalizePhoneNumber)
        }
    }

    private suspend fun callEvent(phoneNumber: String, message: String, type: IncomingCallType) {
        incomingCallSharedFlow.emit(IncomingCallData(
            phoneNumber = phoneNumber,
            message = message
        ))
    }

    suspend fun showSpamBlockedNotification(context: Context, normalizePhoneNumber: String) {
        val channelId = "safenest_spam_alerts"
        getCallerIdUseCase(normalizePhoneNumber)?.let {

            val channel = NotificationChannel(
                channelId,
                "Spam Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            val intent = routerManager.getLaunchIntent(
                CallDetectionDeeplink.entryPointMissingCall(
                    callerIdInfo = it
                )
            )
            val notificationId = System.currentTimeMillis().toInt()
            intent?.putExtra("EXTRA_NOTIFICATION_ID", notificationId)

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_alert_spam)

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alert_spam)
                .setLargeIcon(largeIcon)
                .setContentTitle("Spam caller blocked")
                .setContentText("SafeNest identified & blocked ${it.phoneNumber} as spam but you might want to check because they call you twice today")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("SafeNest identified & blocked ${it.phoneNumber} as spam but you might want to check because they call you twice today")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .addAction(0, "View Details", pendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

    }
}