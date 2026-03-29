package com.safeNest.demo.features.urlGuard.impl.urlGuard.view

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import com.safeNest.demo.features.urlGuard.impl.R
import com.safeNest.demo.features.urlGuard.impl.urlGuard.mapper.toBlockingText
import com.safeNest.demo.features.urlGuard.impl.urlGuard.util.CardPositionCalculator
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.SecureView.Companion.TOAST_TOOLTIP_DURATION_MS
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.floatingbutton.FloatingView
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.DetectionStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.FloatingButtonFeature
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.toActionCarViewIconBgColorRes
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.toActionCardViewIcon
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model.toActionCardViewLabel

/**
 * Orchestrates three system-overlay layers drawn on top of every app.
 *
 * Layer stack (bottom → top):
 * ┌─────────────────────────────────────────────────────┐
 * │  [1] BlockingPageView    — fullscreen danger page   │  pre-added as GONE; shown on demand
 * │  [2] FloatingView        — draggable shield button  │  always visible after show()
 * │  [3] QuickActionCardView — tooltip on button tap    │  toggles on tap
 * └─────────────────────────────────────────────────────┘
 *
 * **Z-order guarantee**: [BlockingPageView] is added to the [WindowManager] first
 * (in [showFirstTime]), so [FloatingView] is always rendered on top even when the blocking
 * page is visible.  Showing/hiding the blocking page only toggles its [View.VISIBLE]
 * / [View.GONE] state and the [WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE] flag —
 * the button is never hidden.
 *
 * The [QuickActionCardView] behaves as a **tooltip** anchored near the
 * [com.safeNest.demo.features.urlGuard.impl.urlGuard.view.floatingbutton.FloatingView]: tapping the button shows/hides it; it automatically
 * appears on the opposite side of the screen so it never goes off-screen.
 *
 */
class SecureView(
    private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val displayMetrics = DisplayMetrics().also {
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(it)
    }

    // ── Layer 1: Blocking page (fullscreen, shown on demand) ──────────────────

    val blockingPage = BlockingPageView(context)

    // ── Layer 2: Floating button (always visible after show()) ─────────────────

    private val floatingView = FloatingView(context)

    // ── Layer 3: Alert tooltip (shown when FloatingView is tapped) ────────────

    val actionCard = QuickActionCardView(context)

    private var isActionCardShown  = false
    private var actionCardParams   : WindowManager.LayoutParams? = null
    private var currentFeature    : FloatingButtonFeature = FloatingButtonFeature.DEFAULT

    // ── Layer 4: Toast tooltip (text-only, auto-dismisses after 1 s) ──────────

    private val toastTooltip       = ToastTooltipView(context)
    private var isToastTooltipShown = false
    private val toastHandler        = Handler(Looper.getMainLooper())
    private val dismissToastRunnable = Runnable { hideToastTooltip() }

    /** True while the fullscreen blocking page is visible on screen. */
    val isBlockingPageVisible: Boolean
        get() = blockingPage.isVisible

    // ── Callbacks ─────────────────────────────────────────────────────────────

    /** Forwarded to [BlockingPageView.onGoBackClick]. */
    var onGoBackClick: (() -> Unit)? = null

    /** Forwarded to [BlockingPageView.onProceedAnywayClick]. */
    var onProceedAnywayClick: (() -> Unit)? = null

    /**
     * Called every time the user taps the floating button.
     * The host (service) is responsible for deciding what to do based on the
     * current [ScreenSurface]; [SecureView] no longer auto-toggles the action card.
     */
    var onFloatingButtonClick: (() -> Unit)? = null
    var onFloatingButtonLongClick: (() -> Unit)? = null

    //

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        // Tap FloatingView → delegate to host via callback
        floatingView.setOnClickListener { onFloatingButtonClick?.invoke() }
        floatingView.setOnLongPressListener { onFloatingButtonLongClick?.invoke() }
        // Wire blocking-page buttons through to our public callbacks
        blockingPage.onGoBackClick        = { onGoBackClick?.invoke() }
        blockingPage.onProceedAnywayClick = { onProceedAnywayClick?.invoke() }

        toastTooltip.setOnClickListener {
            hideToastTooltip()
        }
    }

    // ── Public API: lifecycle ─────────────────────────────────────────────────

    /**
     * Add all overlay layers to the WindowManager.
     * Call this once when the service starts.
     *
     * **Order matters for Z-layering**: [blockingPage] is added first so it sits
     * below [FloatingView]. The floating button is therefore always visible on top,
     * even when the blocking page is displayed.
     */
    fun showFirstTime() {
        // ── Layer 1: BlockingPageView — added FIRST so it renders below FloatingView ──
        // Start touch-transparent (FLAG_NOT_TOUCHABLE) and invisible (GONE).
        // showBlockingPage() / hideBlockingPage() toggle these; the view is never
        // re-added or removed from the WindowManager after this point.
        val blockingParams = BlockingPageView.fullScreenParams().apply {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        windowManager.addView(blockingPage, blockingParams)
        blockingPage.visibility = View.GONE

        // ── Layer 2: FloatingView — added AFTER so it renders on top ──────────────
        floatingView.setMoveDirection(0)    // snap to nearest edge after drag
        floatingView.setIdleAlpha(0.5f)     // 50 % transparent when idle
        floatingView.show()
    }

    /**
     * Remove all overlays (button, tooltip, blocking page) from the screen.
     * Call this in [Service.onDestroy].
     */
    fun dismiss() {
        hideActionCard()
        hideToastTooltip()
        // blockingPage was pre-added in show() — remove it directly.
        if (blockingPage.isAttachedToWindow) {
            try { windowManager.removeView(blockingPage) } catch (e: Exception) {
                Log.w(TAG, "dismiss: could not remove blockingPage", e)
            }
        }
        floatingView.hide()
    }

    // ── Public API: floating button ───────────────────────────────────────────

    /**
     * Make the floating button visible.
     * Safe to call repeatedly — no-op if the button is already visible.
     * Requires [showFirstTime] to have been called at least once beforehand.
     */
    fun showFloatingButton() {
        floatingView.showInWindow()
    }

    /**
     * Hide the floating button and dismiss the action card if open.
     * Safe to call repeatedly — no-op if the button is already hidden.
     */
    fun hideFloatingButton() {
        hideActionCard()
        floatingView.hideInWindow()
    }

    /**
     * Update the floating button's icon and background colour.
     *
     * @param feature Determines the icon (shield, globe, phone, message).
     * @param status  Determines the background color (unknown/safe/warning/dangerous).
     */
    fun updateButton(feature: FloatingButtonFeature, status: DetectionStatus) {
        currentFeature = feature
        floatingView.update(feature, status)
    }

    // ── Public API: floating button loading state ─────────────────────────────

    /**
     * Show an indeterminate spinner on the floating button, replacing the icon.
     * The previous icon and status colour are saved and restored by [hideButtonLoading].
     */
    fun showButtonLoading() = floatingView.showLoading()

    /**
     * Remove the loading spinner and restore the floating button to the state
     * it was in before [showButtonLoading] was called.
     */
    fun hideButtonLoading() = floatingView.hideLoading()

    // ── Public API: quick action card ─────────────────────────────────────────────

    fun updateActionCard(
        feature: FloatingButtonFeature,
        status: DetectionStatus,
        actions: List<QuickActionCardView.Action>) {
        feature.toActionCardViewLabel(context)?.let {
            actionCard.setAlertLabel(it)
        }
        if (actions.isNotEmpty()) actionCard.setActions(actions)

        feature.toActionCardViewIcon(context).let {
            actionCard.setAlertIconDrawable(it)
        }
        status.toActionCarViewIconBgColorRes().let { colorRes ->
            val color = ContextCompat.getColor(context, colorRes)
            actionCard.setAlertInnerBackground(color)
            actionCard.setAlertLabelColor(color)
        }
    }

    fun showActionCard() {
        if (isActionCardShown) return     // already visible — content already updated above

        val p = buildOverlayParams(actionCard)
        windowManager.addView(actionCard, p)
        actionCardParams   = p
        isActionCardShown  = true
    }

    /**
     * Remove the alert tooltip from the screen.
     * No-op if the tooltip is not currently showing.
     */
    fun hideActionCard() {
        if (!isActionCardShown) return
        try { windowManager.removeView(actionCard) } catch (e: Exception) {
            Log.w(TAG, "hideAlertCard: removeView failed", e)
        }
        isActionCardShown = false
        actionCardParams  = null
    }

    // ── Public API: toast tooltip ─────────────────────────────────────────────

    /**
     * Show a text-only tooltip anchored next to the floating button.
     *
     * - Uses the same [CardPositionCalculator] algorithm as the action card so it
     *   never overlaps the button or goes off-screen.
     * - If a tooltip is already visible it is dismissed first (no stacking).
     * - The tooltip disappears automatically after [TOAST_TOOLTIP_DURATION_MS].
     */
    fun showToastTooltip(text: CharSequence) {
        hideToastTooltip()
        toastTooltip.setText(text)
        val p = buildOverlayParams(toastTooltip, abovePriority = true)
        windowManager.addView(toastTooltip, p)
        isToastTooltipShown = true
        toastHandler.postDelayed(dismissToastRunnable, TOAST_TOOLTIP_DURATION_MS)
    }

    /**
     * Dismiss the toast tooltip immediately.
     * No-op if the tooltip is not currently shown.
     */
    fun hideToastTooltip() {
        toastHandler.removeCallbacks(dismissToastRunnable)
        if (!isToastTooltipShown) return
        try { windowManager.removeView(toastTooltip) } catch (e: Exception) {
            Log.w(TAG, "hideToastTooltip: removeView failed", e)
        }
        isToastTooltipShown = false
    }

    // ── Public API: blocking page ─────────────────────────────────────────────

    fun updateBLockingPage(
        floatingButtonFeature: FloatingButtonFeature,
        detectionStatus: DetectionStatus,
        analyzeMode: AnalyzeMode,
        reason: String,
        url: String) {
        if(floatingButtonFeature != FloatingButtonFeature.SAFE_BROWSING) return
        blockingPage.setBlockedUrl(url)
        blockingPage.setSecurityAlertTextColor(ContextCompat.getColor(context, detectionStatus.colorRes))
        val bgColor = ContextCompat.getColor(context, detectionStatus.colorRes)
        blockingPage.setAlertInnerBackground(bgColor)
        when(detectionStatus) {
            DetectionStatus.WARNING -> {
                val drawable = ContextCompat.getDrawable(context, floatingButtonFeature.iconRes)?.mutate()
                val color = ContextCompat.getColor(context, R.color.blocking_primary_text)
                drawable?.setTint(color)
                blockingPage.setAlertIconDrawable(drawable)
                val title = context.getString(R.string.high_risk_suspicious_form_detected) + " (${analyzeMode.toBlockingText()})"
                blockingPage.setTitle(title)
                blockingPage.setDescription(reason)
            }

            DetectionStatus.DANGEROUS -> {
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_threat_alert_octagon_indigo)?.mutate()
                val color = ContextCompat.getColor(context, R.color.blocking_primary_text)
                drawable?.setTint(color)
                blockingPage.setAlertIconDrawable(drawable)
                val title = context.getString(R.string.high_risk_scam_detected) + " (${analyzeMode.toBlockingText()})"
                blockingPage.setTitle(title)
                blockingPage.setDescription(reason)
            }
            else -> null
        }
    }



    /**
     * Show the fullscreen blocking page.
     */
    fun showBlockingPage() {
        hideActionCard()                         // dismiss tooltip if open

        // Remove FLAG_NOT_TOUCHABLE so the blocking page intercepts touches,
        // then reveal it. FloatingView stays on top — no need to hide the button.
        val params = blockingPage.layoutParams as WindowManager.LayoutParams
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        windowManager.updateViewLayout(blockingPage, params)
        blockingPage.visibility = View.VISIBLE
    }

    /**
     * Hide the blocking page.
     *
     * The view is made touch-transparent ([FLAG_NOT_TOUCHABLE]) and [View.GONE]
     * so it no longer intercepts touches while remaining attached to the
     * [WindowManager] (preserving the correct Z-order for the next show).
     * The [FloatingView] was never hidden, so nothing needs to be restored.
     */
    fun hideBlockingPage() {
        if (!blockingPage.isAttachedToWindow) return
        val params = blockingPage.layoutParams as WindowManager.LayoutParams
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        windowManager.updateViewLayout(blockingPage, params)
        blockingPage.visibility = View.GONE
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Toggle the quick-actions card on each tap of the floating button. */
    private fun toggleAlertCard() {
        if (!currentFeature.hasQuickActions) return
        if (isActionCardShown) hideActionCard() else showActionCard()
    }

    /**
     * Build [WindowManager.LayoutParams] for any overlay card anchored next to the
     * floating button.
     *
     * Delegates all positioning logic to [CardPositionCalculator], which runs a
     * two-phase "Push & Shift" algorithm:
     *
     *  Phase 1 – try placing the card to the LEFT or RIGHT of the button.
     *    • Preferred side = opposite of the screen-half the button is in.
     *    • If the card overflows → flip to the other side.
     *    • Y is aligned with the button centre; pushed up/down if it overlaps
     *      the button or overflows the screen.
     *
     *  Phase 2 – if neither horizontal side fits, try placing the card ABOVE or
     *    BELOW the button.
     *
     *  Phase 3 – clamp to screen bounds as a last resort.
     *
     * When [abovePriority] is true the algorithm skips Phase 1 and tries to place
     * the card ABOVE the button first (then BELOW, then LEFT/RIGHT).
     *
     * [view] is measured here so the algorithm has its exact pixel dimensions.
     */
    private fun buildOverlayParams(view: View, abovePriority: Boolean = false): WindowManager.LayoutParams {
        val maxW = (displayMetrics.widthPixels  * 0.80f).toInt()
        val maxH = (displayMetrics.heightPixels * 0.60f).toInt()
        view.measure(
            View.MeasureSpec.makeMeasureSpec(maxW, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(maxH, View.MeasureSpec.AT_MOST)
        )

        val btnParams = floatingView.windowLayoutParams
        val placement = CardPositionCalculator.resolve(
            btnX          = btnParams.x,
            btnY          = btnParams.y,
            btnWidth      = btnParams.width,
            btnHeight     = btnParams.height,
            cardWidth     = view.measuredWidth,
            cardHeight    = view.measuredHeight,
            screenWidth   = displayMetrics.widthPixels,
            screenHeight  = displayMetrics.heightPixels,
            density       = displayMetrics.density,
            abovePriority = abovePriority
        )

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // FLAG_NOT_TOUCH_MODAL: touches outside the card pass through to
            // the underlying app; the card itself still receives touch events
            // so its action buttons are fully clickable.
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x       = placement.x
            y       = placement.y
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * displayMetrics.density + 0.5f).toInt()

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "SecureView"

        /** How long the toast tooltip stays on screen before auto-dismissing. */
        private const val TOAST_TOOLTIP_DURATION_MS = 10_000L
    }
}
