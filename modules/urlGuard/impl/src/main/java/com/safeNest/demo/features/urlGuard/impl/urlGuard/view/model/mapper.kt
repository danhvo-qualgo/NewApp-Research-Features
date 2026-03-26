package com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.safeNest.demo.features.commonAndroid.openApp
import com.safeNest.demo.features.commonAndroid.openAppSettings
import com.safeNest.demo.features.urlGuard.impl.R
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.QuickActionCardView.Action


// ── Public API: action card view mapper ─────────────────────────────────────────────
fun FloatingButtonFeature.toActionCardViewLabel(context: Context): CharSequence? {
    return when(this) {
        FloatingButtonFeature.DEFAULT -> context.getString(R.string.blocking_security_alert)
        FloatingButtonFeature.APP_CHECK -> context.getString(R.string.blocking_security_alert)
        FloatingButtonFeature.SAFE_BROWSING -> context.getString(R.string.blocking_security_alert)
        FloatingButtonFeature.CALL_PROTECTION -> context.getString(R.string.blocking_security_alert)
        FloatingButtonFeature.SMS_CHECK -> context.getString(R.string.sms_is_suspicious)
    }
}

fun FloatingButtonFeature.toActionCardViewListAction(
    context: Context,
    status: DetectionStatus,
    data: Any? = null,
    openDetailAction: () -> Unit = {}
): List<Action> {
    fun detailsAction() = Action(
        icon = ContextCompat.getDrawable(context, R.drawable.ic_shield_zap)?.apply {
            setTint(ContextCompat.getColor(context, R.color.blocking_primary))
        }!!,
        title = context.getString(R.string.action_view_details),
        onClick = {
            Log.d("FloatingAction", "Open detail")
            openDetailAction()
        }
    )
    fun openKinShieldAction() = Action(
        icon = ContextCompat.getDrawable(context, R.drawable.ic_shield_zap)?.apply {
            setTint(ContextCompat.getColor(context, R.color.blocking_primary))
        }!!,
        title = context.getString(R.string.action_open_kinshield),
        onClick = {
            Log.d("FloatingAction", "Open kinShield app")
            context.openApp()
        }
    )

    return when (this) {
        FloatingButtonFeature.SAFE_BROWSING -> when (status) {
            DetectionStatus.DANGEROUS,
            DetectionStatus.WARNING,
            DetectionStatus.UNKNOWN -> listOf(detailsAction())
            DetectionStatus.SAFE    -> listOf(openKinShieldAction())
        }

        FloatingButtonFeature.SMS_CHECK -> when (status) {
            DetectionStatus.DANGEROUS -> listOf(
                detailsAction(),
                Action(
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_threat_eye)?.apply {
                        setTint(ContextCompat.getColor(context, R.color.blocking_primary))
                    }!!,
                    title = context.getString(R.string.action_view_notification),
                    onClick = {}
                )
            )
            else -> listOf(openKinShieldAction())
        }

        FloatingButtonFeature.CALL_PROTECTION -> listOf(openKinShieldAction())

        FloatingButtonFeature.APP_CHECK,
        FloatingButtonFeature.DEFAULT -> when (status) {
            DetectionStatus.DANGEROUS,
            DetectionStatus.WARNING-> listOf(
                Action(
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_threat_alert_octagon_indigo)?.apply {
                        setTint(ContextCompat.getColor(context, R.color.blocking_primary))
                    }!!,
                    title = context.getString(R.string.action_open_system_settings),
                    onClick = {
                        val pkg = data as? String
                        pkg?.let {
                            Log.d("FloatingAction", "Open app setting")
                            context.openAppSettings(pkg)
                        }
                    }
                )
            )
            else -> listOf(openKinShieldAction())
        }
    }
}

fun FloatingButtonFeature.toActionCardViewIcon(context: Context): Drawable? {
    return ContextCompat.getDrawable(context,this.iconRes)
}

fun DetectionStatus.toActionCarViewIconBgColorRes(): Int {
    return this.colorRes
}