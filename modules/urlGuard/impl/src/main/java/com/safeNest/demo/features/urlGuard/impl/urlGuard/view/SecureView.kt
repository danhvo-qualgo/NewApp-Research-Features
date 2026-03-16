package com.safeNest.demo.features.urlGuard.impl.urlGuard.view

import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.safeNest.demo.features.urlGuard.impl.urlGuard.DetectionStatus
import com.safeNest.demo.features.urlGuard.impl.urlGuard.FloatingButtonFeature
import com.safeNest.demo.features.urlGuard.impl.urlGuard.view.floatingbutton.FloatingView

/**
 * Orchestrates three system-overlay layers drawn on top of every app.
 *
 * Layer stack (bottom → top):
 * ┌─────────────────────────────────────────────────────┐
 * │  [1] BlockingPageView    — fullscreen danger page   │  pre-added as GONE; shown on demand
 * │  [2] FloatingView        — draggable shield button  │  always visible after show()
 * │  [3] ThreatAlertCardView — tooltip on button tap    │  toggles on tap
 * └─────────────────────────────────────────────────────┘
 *
 * **Z-order guarantee**: [BlockingPageView] is added to the [WindowManager] first
 * (in [show]), so [FloatingView] is always rendered on top even when the blocking
 * page is visible.  Showing/hiding the blocking page only toggles its [View.VISIBLE]
 * / [View.GONE] state and the [WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE] flag —
 * the button is never hidden.
 *
 * The [ThreatAlertCardView] behaves as a **tooltip** anchored near the
 * [com.safeNest.demo.features.urlGuard.impl.urlGuard.view.floatingbutton.FloatingView]: tapping the button shows/hides it; it automatically
 * appears on the opposite side of the screen so it never goes off-screen.
 *
 * ---
 * ### Typical usage inside a Service / AccessibilityService
 * ```kotlin
 * // onCreate
 * val secureView = SecureView(this)
 * secureView.onGoBackClick        = { secureView.hideBlockingPage() }
 * secureView.onProceedAnywayClick = { secureView.hideBlockingPage() }
 * secureView.show()
 *
 * // When a threat is detected
 * secureView.updateButton(FloatingButtonFeature.SAFE_BROWSING, DetectionStatus.DANGEROUS)
 * secureView.showAlertCard(
 *     label = "High-Risk URL Detected",
 *     actions = listOf(
 *         ThreatAlertCardView.Action(icon = ..., title = "Block site") {
 *             secureView.showBlockingPage(url)
 *         }
 *     )
 * )
 *
 * // onDestroy
 * secureView.dismiss()
 * ```
 */
class SecureView(context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val displayMetrics = DisplayMetrics().also {
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(it)
    }

    // ── Layer 1: Blocking page (fullscreen, shown on demand) ──────────────────

    /**
     * The fullscreen danger page. Configure callbacks before calling [show]:
     * ```kotlin
     * secureView.blockingPage.onGoBackClick = { ... }
     * ```
     * Or use the convenience [onGoBackClick] / [onProceedAnywayClick] properties.
     */
    val blockingPage = BlockingPageView(context)

    // ── Layer 2: Floating button (always visible after show()) ─────────────────

    private val floatingView = FloatingView(context)

    // ── Layer 3: Alert tooltip (shown when FloatingView is tapped) ────────────

    /**
     * The threat-alert card that pops up as a tooltip when the floating button
     * is tapped. Populate it before calling [showAlertCard], or configure it
     * directly and call [showAlertCard] without arguments to use whatever was
     * already set.
     */
    val alertCard = ThreatAlertCardView(context)

    private var isAlertCardShown  = false
    private var alertCardParams   : WindowManager.LayoutParams? = null

    // ── Callbacks ─────────────────────────────────────────────────────────────

    /** Forwarded to [BlockingPageView.onGoBackClick]. */
    var onGoBackClick: (() -> Unit)? = null

    /** Forwarded to [BlockingPageView.onProceedAnywayClick]. */
    var onProceedAnywayClick: (() -> Unit)? = null

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        // Tap FloatingView → toggle the alert tooltip
        floatingView.setOnClickListener { toggleAlertCard() }

        // Wire blocking-page buttons through to our public callbacks
        blockingPage.onGoBackClick        = { onGoBackClick?.invoke() }
        blockingPage.onProceedAnywayClick = { onProceedAnywayClick?.invoke() }
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
    fun show() {
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
        hideAlertCard()
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
     * Update the floating button's icon and background colour.
     *
     * @param feature Determines the icon (shield, globe, phone, message).
     * @param status  Determines the background colour (unknown/safe/warning/dangerous).
     */
    fun updateButton(feature: FloatingButtonFeature, status: DetectionStatus) {
        floatingView.update(feature, status)
    }

    // ── Public API: alert tooltip ─────────────────────────────────────────────

    /**
     * Show the [ThreatAlertCardView] as a tooltip near the floating button.
     *
     * The card is positioned on the **opposite side** of the screen from the
     * button so it is always fully visible. If the card is already showing,
     * only its content is refreshed.
     *
     * @param label   Bold header text, e.g. "High-Risk URL Detected".
     * @param actions Action rows to display. Pass an empty list for a header-only card.
     */
    fun showAlertCard(
        label  : CharSequence,
        actions: List<ThreatAlertCardView.Action> = emptyList()
    ) {
        alertCard.setAlertLabel(label)
        alertCard.setActions(actions)

        if (isAlertCardShown) return     // already visible — content already updated above

        val p = buildTooltipParams()
        windowManager.addView(alertCard, p)
        alertCardParams   = p
        isAlertCardShown  = true
    }

    /**
     * Remove the alert tooltip from the screen.
     * No-op if the tooltip is not currently showing.
     */
    fun hideAlertCard() {
        if (!isAlertCardShown) return
        try { windowManager.removeView(alertCard) } catch (e: Exception) {
            Log.w(TAG, "hideAlertCard: removeView failed", e)
        }
        isAlertCardShown = false
        alertCardParams  = null
    }

    // ── Public API: blocking page ─────────────────────────────────────────────

    /**
     * Show the fullscreen blocking page.
     *
     * The [FloatingView] remains visible on top — it was added to the
     * [WindowManager] after [blockingPage] so it always has a higher Z-order.
     *
     * @param url         URL that was blocked — shown in the URL bar on the page.
     * @param title       Bold heading on the blocking page.
     * @param description Body text below the heading.
     */
    fun showBlockingPage(
        url        : String,
        title      : CharSequence = "High Risk: Scam Detected",
        description: CharSequence = "SafeNest has blocked this site to protect you."
    ) {
        hideAlertCard()                         // dismiss tooltip if open
        blockingPage.setBlockedUrl(url)
        blockingPage.setTitle(title)
        blockingPage.setDescription(description)

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

    /** Toggle the alert tooltip on each tap of the floating button. */
    private fun toggleAlertCard() {
        if (isAlertCardShown) hideAlertCard() else showAlertCard("")
    }

    /**
     * Build [WindowManager.LayoutParams] for the alert tooltip.
     *
     * Positioning logic:
     *  - Y is aligned with the floating button's current Y position.
     *  - If the button is on the **right** half of the screen → the card is
     *    anchored to the **left** edge (gravity START).
     *  - If the button is on the **left** half of the screen → the card is
     *    anchored to the **right** edge (gravity END).
     *
     * This ensures the card never overlaps the button and never goes off-screen.
     */
    private fun buildTooltipParams(): WindowManager.LayoutParams {
        val btnX    = floatingView.windowLayoutParams.x
        val btnY    = floatingView.windowLayoutParams.y
        val screenW = displayMetrics.widthPixels
        val margin  = dpToPx(8)

        val gravity: Int
        val x: Int

        if (btnX > screenW / 2) {
            // Button is on the right side → anchor card to the left
            gravity = Gravity.TOP or Gravity.START
            x       = margin
        } else {
            // Button is on the left side → anchor card to the right
            gravity = Gravity.TOP or Gravity.END
            x       = margin
        }

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
            this.gravity = gravity
            this.x       = x
            this.y       = btnY
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * displayMetrics.density + 0.5f).toInt()

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "SecureView"
    }
}
