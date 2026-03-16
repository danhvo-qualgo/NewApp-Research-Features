package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
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
 *
 * ---
 * Typical usage inside the service:
 * ```kotlin
 * private var blockingView: BlockingPageView? = null
 *
 * fun showBlockingPage(url: String) {
 *     if (blockingView?.isAttachedToWindow == true) return
 *     val view = BlockingPageView(this).apply {
 *         setBlockedUrl(url)
 *         setTitle("High Risk: Scam Detected")
 *         setDescription("SafeNest blocked this site…")
 *         setAlertIconDrawable(ContextCompat.getDrawable(this@Service, R.drawable.ic_custom))
 *         onGoBackClick        = { hideBlockingPage() }
 *         onProceedAnywayClick = { hideBlockingPage() }
 *     }
 *     windowManager?.addView(view, BlockingPageView.fullScreenParams())
 *     blockingView = view
 * }
 *
 * fun hideBlockingPage() {
 *     blockingView?.dismiss(windowManager!!)
 *     blockingView = null
 * }
 * ```
 */
class BlockingPageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    // ── Callbacks ─────────────────────────────────────────────────────────────

    /** Called when the user taps "Go back to Safety". */
    var onGoBackClick: (() -> Unit)? = null

    /** Called when the user taps "I understand the risk, proceed anyway". */
    var onProceedAnywayClick: (() -> Unit)? = null

    // ── Child view references ─────────────────────────────────────────────────

    private val frameAlertIconOuter: FrameLayout
    private val frameAlertIconInner: FrameLayout
    private val ivAlertIcon: ImageView
    private val tvTitle: TextView
    private val tvDescription: TextView
    private val tvBlockedUrl: TextView

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        LayoutInflater.from(context).inflate(R.layout.blocking_page, this, true)

        frameAlertIconOuter = findViewById(R.id.frame_alert_icon_outer)
        frameAlertIconInner = findViewById(R.id.frame_alert_icon_inner)
        ivAlertIcon         = findViewById(R.id.iv_alert_icon)
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

    /**
     * Replaces the alert icon image (the icon shown inside the red circle).
     *
     * Can be called at any time — including after [show].
     */
    fun setAlertIconDrawable(drawable: Drawable?) {
        ivAlertIcon.setImageDrawable(drawable)
    }

    /**
     * Replaces the alert icon image by resource id.
     *
     * Can be called at any time — including after [show].
     */
    fun setAlertIconRes(@DrawableRes resId: Int) {
        ivAlertIcon.setImageResource(resId)
    }

    /**
     * Replaces the **outer** circle background (the large, lightly-tinted circle).
     *
     * Pass `null` to clear the background entirely.
     * Can be called at any time — including after [show].
     */
    fun setAlertOuterBackground(drawable: Drawable?) {
        frameAlertIconOuter.background = drawable
    }

    /**
     * Replaces the **inner** circle background (the solid-coloured circle behind the icon).
     *
     * Pass `null` to clear the background entirely.
     * Can be called at any time — including after [show].
     */
    fun setAlertInnerBackground(drawable: Drawable?) {
        frameAlertIconInner.background = drawable
    }

    // ── Text setters ──────────────────────────────────────────────────────────

    /**
     * Replaces the bold title text ("High Risk: Scam Detected" by default).
     *
     * Can be called at any time — including after [show].
     */
    fun setTitle(text: CharSequence) {
        tvTitle.text = text
    }

    /**
     * Replaces the bold title text by string resource id.
     *
     * Can be called at any time — including after [show].
     */
    fun setTitle(@StringRes resId: Int) {
        tvTitle.setText(resId)
    }

    /**
     * Replaces the body description text.
     *
     * Can be called at any time — including after [show].
     */
    fun setDescription(text: CharSequence) {
        tvDescription.text = text
    }

    /**
     * Replaces the body description text by string resource id.
     *
     * Can be called at any time — including after [show].
     */
    fun setDescription(@StringRes resId: Int) {
        tvDescription.setText(resId)
    }

    /**
     * Updates the URL displayed inside the URL bar row.
     *
     * Can be called at any time — including after [show].
     */
    fun setBlockedUrl(url: String) {
        tvBlockedUrl.text = url
    }

    // ── WindowManager helpers ─────────────────────────────────────────────────

    /**
     * Adds this view to [windowManager] as a fullscreen overlay.
     *
     * No-op when the view is already attached — safe to call multiple times.
     */
    fun show(windowManager: WindowManager) {
        if (isAttachedToWindow) return
        windowManager.addView(this, fullScreenParams())
    }

    /**
     * Removes this view from [windowManager].
     *
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
         *
         * Flags:
         * - [WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN]  — extend into
         *   system-bar areas so nothing peeks through at the edges.
         * - [WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS] — lets
         *   the view draw its own opaque background behind the status/nav bars.
         * - [PixelFormat.OPAQUE] — no transparency needed; avoids alpha compositing
         *   overhead and prevents content from bleeding through.
         *
         * Note: [WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE] is intentionally
         * **omitted** so the buttons on the page receive touch and focus events.
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
