package com.safeNest.demo.features.urlGuard.impl.urlGuard.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.safeNest.demo.features.urlGuard.impl.R

/**
 * A lightweight text-only tooltip that renders alongside the floating button.
 *
 * Visual design mirrors [QuickActionCardView]:
 *  - Same [R.drawable.bg_threat_card] background (white, 16dp corners, 1dp border)
 *  - Same elevation and padding rhythm
 *
 * Content is intentionally minimal — a single line/multi-line [CharSequence].
 * Showing, positioning, and auto-dismissal are managed by [SecureView].
 */
class ToastTooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val tvMessage: TextView

    init {
        orientation = HORIZONTAL
        background = ContextCompat.getDrawable(context, R.drawable.bg_threat_card)
        elevation = dpToPx(8).toFloat()
        clipToOutline = true

        val padH = dpToPx(16)
        val padV = dpToPx(12)
        setPadding(padH, padV, padH, padV)

        tvMessage = TextView(context).apply {
            setTextColor(ContextCompat.getColor(context, R.color.threat_action_text))
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 4
        }
        addView(tvMessage)
    }

    fun setText(text: CharSequence) {
        tvMessage.text = text
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()
}
