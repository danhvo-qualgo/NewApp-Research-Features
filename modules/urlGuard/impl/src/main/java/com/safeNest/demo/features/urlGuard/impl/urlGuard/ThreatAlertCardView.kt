package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.safeNest.demo.features.urlGuard.impl.R

/**
 * Self-contained threat alert card that matches the Figma "Select card" design:
 *
 * ```
 * ┌──────────────────────────────────────────┐
 * │  [●] SMS IS SUSPICIOUS                   │  ← header (icon + alert label)
 * │  ──────────────────────────────────────  │  ← divider
 * │  [⬡]  Scam detail                        │  ← action rows (dynamic)
 * │  [👁]  View SMS                           │
 * └──────────────────────────────────────────┘
 * ```
 *
 * Both the **header** (icon cluster + label) and the **action list** can be
 * changed at any time — before or after the view is attached to a window.
 *
 * ---
 * ### Usage
 * ```kotlin
 * val card = ThreatAlertCardView(context).apply {
 *     setAlertLabel("SMS IS SUSPICIOUS")
 *     setAlertIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_custom_alert))
 *
 *     setActions(listOf(
 *         ThreatAlertCardView.Action(
 *             icon = ContextCompat.getDrawable(context, R.drawable.ic_threat_alert_octagon_indigo)!!,
 *             title = "Scam detail",
 *             onClick = { showScamDetails() }
 *         ),
 *         ThreatAlertCardView.Action(
 *             icon = ContextCompat.getDrawable(context, R.drawable.ic_threat_eye)!!,
 *             title = "View SMS",
 *             onClick = { openSms() }
 *         ),
 *     ))
 * }
 * parentLayout.addView(card)
 * ```
 */
class ThreatAlertCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    // ── Model ─────────────────────────────────────────────────────────────────

    /**
     * Represents one row in the action list.
     *
     * @param icon     Drawable shown in the 24dp icon slot on the left.
     * @param title    Text shown next to the icon (16sp SemiBold, #334155).
     * @param onClick  Optional click callback for the entire row. Pass `null` for non-interactive rows.
     */
    data class Action(
        val icon: Drawable,
        val title: CharSequence,
        val onClick: (() -> Unit)? = null
    )

    // ── Child view references ─────────────────────────────────────────────────

    private val frameAlertOuter: FrameLayout
    private val frameAlertInner: FrameLayout
    private val ivAlertIcon: ImageView
    private val tvAlertLabel: TextView
    private val llActions: LinearLayout

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        // ── Card visual style ─────────────────────────────────────────────────
        orientation = VERTICAL
        background = ContextCompat.getDrawable(context, R.drawable.bg_threat_card)
        elevation = dpToPx(8).toFloat()
        clipToOutline = true             // clip children to rounded-corner outline
        val pad = dpToPx(24)
        setPadding(pad, pad, pad, pad)

        // ── Inflate children via <merge> ──────────────────────────────────────
        LayoutInflater.from(context).inflate(R.layout.threat_alert_card, this, true)

        frameAlertOuter = findViewById(R.id.frame_threat_alert_outer)
        frameAlertInner = findViewById(R.id.frame_threat_alert_inner)
        ivAlertIcon     = findViewById(R.id.iv_threat_alert_icon)
        tvAlertLabel    = findViewById(R.id.tv_threat_alert_label)
        llActions       = findViewById(R.id.ll_threat_actions)
    }

    // ── Header – alert icon setters ───────────────────────────────────────────

    /**
     * Replaces the icon shown inside the red circle in the card header.
     * Safe to call at any time.
     */
    fun setAlertIconDrawable(drawable: Drawable?) {
        ivAlertIcon.setImageDrawable(drawable)
    }

    /** @see setAlertIconDrawable */
    fun setAlertIconRes(@DrawableRes resId: Int) {
        ivAlertIcon.setImageResource(resId)
    }

    /**
     * Replaces the **outer** (lightly tinted) circle background of the header icon cluster.
     * Pass `null` to clear.  Safe to call at any time.
     */
    fun setAlertOuterBackground(drawable: Drawable?) {
        frameAlertOuter.background = drawable
    }

    /**
     * Replaces the **inner** (solid coloured) circle background of the header icon cluster.
     * Pass `null` to clear.  Safe to call at any time.
     */
    fun setAlertInnerBackground(drawable: Drawable?) {
        frameAlertInner.background = drawable
    }

    // ── Header – label setter ─────────────────────────────────────────────────

    /**
     * Replaces the bold alert label text (e.g. "SMS IS SUSPICIOUS").
     * Safe to call at any time.
     */
    fun setAlertLabel(text: CharSequence) {
        tvAlertLabel.text = text
    }

    /** @see setAlertLabel */
    fun setAlertLabel(@StringRes resId: Int) {
        tvAlertLabel.setText(resId)
    }

    // ── Action list ───────────────────────────────────────────────────────────

    /**
     * Replaces the entire action list with [actions].
     *
     * The first item has no top margin; subsequent items are separated by
     * `threat_card_section_spacing` (20dp). Safe to call at any time.
     */
    fun setActions(actions: List<Action>) {
        llActions.removeAllViews()
        actions.forEachIndexed { index, action ->
            llActions.addView(buildActionItemView(action, isFirst = index == 0))
        }
    }

    /**
     * Appends a single [action] to the bottom of the action list.
     * Automatically applies 20dp top margin if other items already exist.
     * Safe to call at any time.
     */
    fun addAction(action: Action) {
        val isFirst = llActions.childCount == 0
        llActions.addView(buildActionItemView(action, isFirst))
    }

    /**
     * Removes all action rows from the list.
     * Safe to call at any time.
     */
    fun clearActions() {
        llActions.removeAllViews()
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun buildActionItemView(action: Action, isFirst: Boolean): View {
        val item = LayoutInflater.from(context)
            .inflate(R.layout.item_threat_alert_action, llActions, false)

        item.findViewById<ImageView>(R.id.iv_action_icon).setImageDrawable(action.icon)
        item.findViewById<TextView>(R.id.tv_action_title).text = action.title

        action.onClick?.let { listener ->
            item.setOnClickListener { listener() }
        }

        // Space between action rows (20dp top margin on every item except the first)
        if (!isFirst) {
            (item.layoutParams as LayoutParams).topMargin = dpToPx(20)
        }

        return item
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()
}
