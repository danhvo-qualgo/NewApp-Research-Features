package com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.safeNest.demo.features.urlGuard.impl.R
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.FloatingButtonFeature
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.QuickActionCardView.Action

fun FloatingButtonFeature.toAlertCardViewLabel(context: Context): CharSequence? {
    return when(this) {
        FloatingButtonFeature.DEFAULT -> null
        FloatingButtonFeature.SAFE_BROWSING -> context.getString(R.string.blocking_security_alert)
        FloatingButtonFeature.CALL_PROTECTION -> null
        FloatingButtonFeature.SMS_CHECK -> context.getString(R.string.sms_is_suspicious)
    }
}

fun FloatingButtonFeature.toAlertCardViewListAction(context: Context): List<Action> {
    return when(this) {
        FloatingButtonFeature.DEFAULT, FloatingButtonFeature.CALL_PROTECTION -> emptyList()
        FloatingButtonFeature.SAFE_BROWSING -> listOf(
            Action(
                icon = ContextCompat.getDrawable(context, R.drawable.ic_shield_zap)?.apply {
                    val color = ContextCompat.getColor(context, R.color.blocking_primary)
                    setTint(color)
                }!!,
                title = context.getString(R.string.check_scam_intent),
                onClick = {}
            ),
            Action(
                icon = ContextCompat.getDrawable(context, R.drawable.ic_camera_01)?.apply {
                    val color = ContextCompat.getColor(context, R.color.blocking_primary)
                    setTint(color)
                }!!,
                title = context.getString(R.string.screenshot),
                onClick = {}
            ),
            Action(
                icon = ContextCompat.getDrawable(context, R.drawable.ic_video_recorder)?.apply {
                    val color = ContextCompat.getColor(context, R.color.blocking_primary)
                    setTint(color)
                }!!,
                title = context.getString(R.string.record),
                onClick = {}
            ),
        )

        FloatingButtonFeature.SMS_CHECK -> listOf(
            Action(
                icon = ContextCompat.getDrawable(context, R.drawable.ic_threat_alert_octagon_indigo)?.apply {
                    val color = ContextCompat.getColor(context, R.color.blocking_primary)
                    setTint(color)
                }!!,
                title = context.getString(R.string.scam_detail),
                onClick = {}
            ),
            Action(
                icon = ContextCompat.getDrawable(context, R.drawable.ic_threat_eye)?.apply {
                    val color = ContextCompat.getColor(context, R.color.blocking_primary)
                    setTint(color)
                }!!,
                title = context.getString(R.string.view_sms),
                onClick = {}
            )
        )
    }
}

fun FloatingButtonFeature.toAlertCardViewIcon(context: Context): Drawable? {
    return when(this) {
        FloatingButtonFeature.DEFAULT -> null
        FloatingButtonFeature.SAFE_BROWSING -> ContextCompat.getDrawable(context,this.iconRes)
        FloatingButtonFeature.CALL_PROTECTION -> null
        FloatingButtonFeature.SMS_CHECK -> ContextCompat.getDrawable(context,this.iconRes)
    }
}

fun DetectionStatus.toAlertCarViewIconBgColor(): Int {
    return this.colorRes
}