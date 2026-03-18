package com.safeNest.demo.features.urlGuard.impl.urlGuard.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.safeNest.demo.features.urlGuard.impl.R

/**
 * Self-contained action card that matches the Figma "Select card" design:
 *
 * ┌──────────────────────────────────────────┐
 * │  [●] SMS IS SUSPICIOUS                   │  ← header (icon + alert label)
 * │  ──────────────────────────────────────  │  ← divider
 * │  [⬡]  Scam detail                        │  ← action rows (dynamic)
 * │  [👁]  View SMS                           │
 * └──────────────────────────────────────────┘
 */
class QuickActionCardView @JvmOverloads constructor(
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
    private val frameAlertInner: FrameLayout
    private val ivAlertIcon: ImageView
    private val tvAlertLabel: TextView
    private val llActions: LinearLayout

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        orientation = VERTICAL
        background = ContextCompat.getDrawable(context, R.drawable.bg_threat_card)
        elevation = dpToPx(8).toFloat()
        clipToOutline = true
        val pad = dpToPx(24)
        setPadding(pad, pad, pad, pad)

        LayoutInflater.from(context).inflate(R.layout.threat_alert_card, this, true)

        frameAlertInner = findViewById(R.id.frame_threat_alert_inner)
        ivAlertIcon     = findViewById(R.id.iv_threat_alert_icon)
        tvAlertLabel    = findViewById(R.id.tv_threat_alert_label)
        llActions       = findViewById(R.id.ll_threat_actions)
    }

    // ── Header – alert icon setters ───────────────────────────────────────────

    fun setAlertIconDrawable(drawable: Drawable?) {
        ivAlertIcon.setImageDrawable(drawable)
    }

    fun setAlertIconRes(@DrawableRes resId: Int) {
        ivAlertIcon.setImageResource(resId)
    }

    fun setAlertInnerBackground(drawable: Drawable?) {
        frameAlertInner.background = drawable
    }

    fun setAlertInnerBackground(@ColorInt color: Int ) {
        (frameAlertInner.background as? GradientDrawable)?.setColor(color)
    }

    // ── Header – label setter ─────────────────────────────────────────────────

    fun setAlertLabel(text: CharSequence) {
        tvAlertLabel.text = text
    }

    fun setAlertLabel(@StringRes resId: Int) {
        tvAlertLabel.setText(resId)
    }

    // ── Action list ───────────────────────────────────────────────────────────

    /**
     * Replaces the entire action list with [actions].
     */
    fun setActions(actions: List<Action>) {
        llActions.removeAllViews()
        actions.forEachIndexed { index, action ->
            llActions.addView(buildActionItemView(action, isFirst = index == 0))
        }
    }

    /**
     * Appends a single [action] to the bottom of the action list.
     */
    fun addAction(action: Action) {
        val isFirst = llActions.childCount == 0
        llActions.addView(buildActionItemView(action, isFirst))
    }

    /**
     * Removes all action rows from the list.
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

        if (!isFirst) {
            (item.layoutParams as LayoutParams).topMargin = dpToPx(20)
        }

        return item
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()
}
