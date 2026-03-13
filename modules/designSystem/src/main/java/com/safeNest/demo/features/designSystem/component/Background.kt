package com.safeNest.demo.features.designSystem.component

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val gradientBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFD5D9F9), // Top color
        Color(0xFFFAFAFA)  // Bottom color
    )
)