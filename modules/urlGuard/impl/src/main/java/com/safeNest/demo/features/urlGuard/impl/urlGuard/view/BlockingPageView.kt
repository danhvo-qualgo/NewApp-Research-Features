package com.safeNest.demo.features.urlGuard.impl.urlGuard.view

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.safeNest.demo.features.urlGuard.impl.R

/**
 * Fullscreen overlay view that inflates [R.layout.blocking_page].
 *
 * Designed to be added to / removed from a [WindowManager] directly
 * (e.g. from [UrlGuardAccessibilityService] or any service that holds
 * the SYSTEM_ALERT_WINDOW / ACTION_MANAGE_OVERLAY_PERMISSION permission).
 *
 * All setter functions are safe to call **before or after** [show] —
 * they update the live view hierarchy immediately on whichever thread
 * they are called from (use the main thread when the view is visible).
 * ```
 */
class BlockingPageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    // ── Callbacks ─────────────────────────────────────────────────────────────

    var onGoBackClick: (() -> Unit)? = null

    var onProceedAnywayClick: (() -> Unit)? = null

    // ── Child view references ─────────────────────────────────────────────────

    private val frameAlertIconInner: FrameLayout
    private val ivAlertIcon: ImageView
    private val securityAlertText: TextView
    private val tvTitle: TextView
    private val tvDescription: TextView
    private val tvBlockedUrl: TextView

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        LayoutInflater.from(context).inflate(R.layout.blocking_page, this, true)

        frameAlertIconInner = findViewById(R.id.frame_alert_icon_inner)
        ivAlertIcon         = findViewById(R.id.iv_alert_icon)
        securityAlertText = findViewById(R.id.security_alert_text)
        tvTitle             = findViewById(R.id.tv_blocking_title)
        tvDescription       = findViewById(R.id.tv_blocking_description)
        tvBlockedUrl        = findViewById(R.id.tv_blocked_url)

        findViewById<View>(R.id.btn_go_back).setOnClickListener {
            onGoBackClick?.invoke()
        }
        findViewById<View>(R.id.tv_proceed_anyway).setOnClickListener {
            onProceedAnywayClick?.invoke()
        }
    }

    // ── Alert icon setters ────────────────────────────────────────────────────

    fun setAlertIconDrawable(drawable: Drawable?) {
        ivAlertIcon.setImageDrawable(drawable)
    }


    fun setAlertIconRes(@DrawableRes resId: Int) {
        ivAlertIcon.setImageResource(resId)
    }


    fun setAlertInnerBackground(drawable: Drawable?) {
        frameAlertIconInner.background = drawable
    }

    fun setAlertInnerBackground(@ColorInt color: Int ) {
        (frameAlertIconInner.background as? GradientDrawable)?.setColor(color)
    }

    // ── Text setters ──────────────────────────────────────────────────────────

    fun setSecurityAlertTextColor(@ColorInt color: Int) {
        securityAlertText.setTextColor(color)
    }

    fun setTitle(text: CharSequence) {
        tvTitle.text = text
    }

    fun setTittleColor(@ColorInt color: Int) {
        tvTitle.setTextColor(color)
    }

    fun setTitle(@StringRes resId: Int) {
        tvTitle.setText(resId)
    }

    fun setDescription(text: CharSequence) {
        tvDescription.text = text
    }

    fun setDescription(@StringRes resId: Int) {
        tvDescription.setText(resId)
    }

    fun setBlockedUrl(url: String) {
        tvBlockedUrl.text = url
    }

    // ── WindowManager helpers ─────────────────────────────────────────────────

    /**
     * Adds this view to [windowManager] as a fullscreen overlay.
     * No-op when the view is already attached — safe to call multiple times.
     */
    fun show(windowManager: WindowManager) {
        if (isAttachedToWindow) return
        windowManager.addView(this, fullScreenParams())
    }

    /**
     * Removes this view from [windowManager].
     * Safe to call even when the view is not currently attached (no-op).
     */
    fun dismiss(windowManager: WindowManager) {
        if (!isAttachedToWindow) return
        try {
            windowManager.removeView(this)
        } catch (e: Exception) {
            Log.w(TAG, "dismiss: removeView failed", e)
        }
    }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "BlockingPageView"

        /**
         * Returns [WindowManager.LayoutParams] for a match-parent
         * [TYPE_APPLICATION_OVERLAY] that covers the full screen.
         */
        fun fullScreenParams(): WindowManager.LayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.OPAQUE
        )
    }
}
