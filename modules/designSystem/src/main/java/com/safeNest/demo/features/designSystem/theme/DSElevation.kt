package com.safeNest.demo.features.designSystem.theme

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DSElevation {

    private val FocusPrimaryColor = Color(22, 141, 113, (255 * 0.15).toInt())
    private val FocusGrayColor = Color(156, 173, 196, (255 * 0.15).toInt())
    private val FocusSuccessColor = Color(22, 141, 113, (255 * 0.15).toInt())
    private val FocusErrorColor = Color(220, 38, 38, (255 * 0.15).toInt())
    private val SoftColor = Color(194, 197, 205, (255 * 0.2).toInt())
    private val HardMdColor = Color(16, 24, 32, (255 * 0.08).toInt())
    private val HardBorderColor = Color(0, 0, 0, (255 * 0.07).toInt())

    /* SHADOWS */

    // --shadow-focus-primary: 0 0 0 3px rgba(22 141 113 / 0.15);
    fun Modifier.shadowFocusPrimary(radius: Dp = 0.dp) = advancedShadow(
        color = FocusPrimaryColor,
        cornersRadius = radius,
        spread = 3.dp,
        blur = 0.dp
    )

    // --shadow-focus-gray: 0 0 0 3px rgba(156 173 196 / 0.15);
    fun Modifier.shadowFocusGray(radius: Dp = 0.dp) = advancedShadow(
        color = FocusGrayColor,
        cornersRadius = radius,
        spread = 3.dp,
        blur = 0.dp
    )

    // --shadow-focus-success: 0 0 0 3px rgba(22 141 113 / 0.15);
    fun Modifier.shadowFocusSuccess(radius: Dp = 0.dp) = advancedShadow(
        color = FocusSuccessColor,
        cornersRadius = radius,
        spread = 3.dp,
        blur = 0.dp
    )

    // --shadow-focus-error: 0 0 0 3px rgba(220 38 38 / 0.15);
    fun Modifier.shadowFocusError(radius: Dp = 0.dp) = advancedShadow(
        color = FocusErrorColor,
        cornersRadius = radius,
        spread = 3.dp,
        blur = 0.dp
    )

    // --shadow-soft-sm: 0 0 20px 0 rgba(194 197 205 / 0.2);
    fun Modifier.shadowSoftSm(radius: Dp = 0.dp) = advancedShadow(
        color = SoftColor,
        cornersRadius = radius,
        spread = 0.dp,
        blur = 20.dp
    )

    // --shadow-soft-md: 0 0 32px 0 rgba(194 197 205 / 0.2);
    fun Modifier.shadowSoftMd(radius: Dp = 0.dp) = advancedShadow(
        color = SoftColor,
        cornersRadius = radius,
        spread = 0.dp,
        blur = 32.dp
    )

    // --shadow-hard-sm: 0 0 20px 0 rgba(194 197 205 / 0.2), 0 0 0 1px rgba(0 0 0 / 0.07);
    fun Modifier.shadowHardSm(radius: Dp = 0.dp) = this
        .advancedShadow(
            color = SoftColor,
            cornersRadius = radius,
            spread = 0.dp,
            blur = 20.dp
        )
        .advancedShadow(
            color = HardBorderColor,
            cornersRadius = radius,
            spread = 1.dp,
            blur = 0.dp
        )

    // --shadow-hard-md: 0 0 32px 0 rgba(16 24 32 / 0.08), 0 0 0 1px rgba(0 0 0 / 0.07);
    fun Modifier.shadowHardMd(radius: Dp = 0.dp) = this
        .advancedShadow(
            color = HardMdColor,
            cornersRadius = radius,
            spread = 0.dp,
            blur = 32.dp
        )
        .advancedShadow(
            color = HardBorderColor,
            cornersRadius = radius,
            spread = 1.dp,
            blur = 0.dp
        )

    private fun Modifier.advancedShadow(
        color: Color,
        cornersRadius: Dp = 0.dp,
        blur: Dp = 0.dp,
        offsetY: Dp = 0.dp,
        offsetX: Dp = 0.dp,
        spread: Dp = 0.dp
    ) = drawBehind {
        val shadowColor = color.toArgb()

        drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = shadowColor

            val dx = offsetX.toPx()
            val dy = offsetY.toPx()
            val spreadPx = spread.toPx()
            val blurPx = blur.toPx()

            if (blurPx > 0) {
                frameworkPaint.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }

            val left = 0f - spreadPx + dx
            val top = 0f - spreadPx + dy
            val right = size.width + spreadPx + dx
            val bottom = size.height + spreadPx + dy

            val radiusPx = cornersRadius.toPx()
            val shadowRadiusPx = if (radiusPx == 0f) 0f else radiusPx + spreadPx

            it.drawRoundRect(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                radiusX = shadowRadiusPx,
                radiusY = shadowRadiusPx,
                paint = paint
            )
        }
    }
}