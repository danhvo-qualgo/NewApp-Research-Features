package com.safeNest.demo.features.designSystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.safeNest.demo.features.designSystem.R

object DSTypography {

    private val baseTextStyle = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.mulish_light, FontWeight.W300),
            Font(R.font.mulish_regular, FontWeight.Normal),
            Font(R.font.mulish_medium, FontWeight.Medium),
            Font(R.font.mulish_semi_bold, FontWeight.SemiBold),
            Font(R.font.mulish_bold, FontWeight.Bold)
        )
    )

    val h1 = DSTextStyle(baseTextStyle.copy(fontSize = 48.sp, lineHeight = 54.sp))
    val h2 = DSTextStyle(baseTextStyle.copy(fontSize = 36.sp, lineHeight = 42.sp))
    val h3 = DSTextStyle(baseTextStyle.copy(fontSize = 30.sp, lineHeight = 36.sp))
    val h4 = DSTextStyle(baseTextStyle.copy(fontSize = 24.sp, lineHeight = 32.sp))
    val subHeadline = DSTextStyle(baseTextStyle.copy(fontSize = 20.sp, lineHeight = 28.sp))
    val body1 = DSTextStyle(baseTextStyle.copy(fontSize = 18.sp, lineHeight = 26.sp))
    val body2 = DSTextStyle(baseTextStyle.copy(fontSize = 16.sp, lineHeight = 24.sp))
    val caption1 = DSTextStyle(baseTextStyle.copy(fontSize = 14.sp, lineHeight = 20.sp))
    val caption2 = DSTextStyle(baseTextStyle.copy(fontSize = 12.sp, lineHeight = 16.sp))
    val caption2Hint = DSTextStyle(
        baseTextStyle.copy(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.W400
        )
    )

    class DSTextStyle(textStyle: TextStyle) {
        val light = textStyle.copy(fontWeight = FontWeight.W300)
        val regular = textStyle.copy(fontWeight = FontWeight.Normal)
        val medium = textStyle.copy(fontWeight = FontWeight.Medium)
        val semiBold = textStyle.copy(fontWeight = FontWeight.SemiBold)
        val bold = textStyle.copy(fontWeight = FontWeight.Bold)
    }
}