package com.safeNest.demo.features.designSystem.component

import androidx.compose.ui.graphics.Brush
import com.safeNest.demo.features.designSystem.theme.color.colorIndigoGradientStart
import com.safeNest.demo.features.designSystem.theme.color.colorWhiteSmoke

val gradientBackground = Brush.verticalGradient(
    colors = listOf(
        colorIndigoGradientStart,
        colorWhiteSmoke,
    )
)