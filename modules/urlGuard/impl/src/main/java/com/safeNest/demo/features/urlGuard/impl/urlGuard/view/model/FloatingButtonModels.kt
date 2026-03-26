package com.safeNest.demo.features.urlGuard.impl.urlGuard.view.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.safeNest.demo.features.urlGuard.impl.R

/** Which feature the floating button is representing — drives the icon displayed. */
enum class FloatingButtonFeature(
    @DrawableRes val iconRes: Int,
    val hasQuickActions: Boolean
) {
    DEFAULT(R.drawable.icon_3_dot, hasQuickActions = false),
    APP_CHECK(R.drawable.icon_3_dot, hasQuickActions = true),
    SMS_CHECK(R.drawable.icon_message, hasQuickActions = true),
    CALL_PROTECTION(R.drawable.icon_callprotection, hasQuickActions = true),
    SAFE_BROWSING(R.drawable.icon_safebrowsing, hasQuickActions = true)
}

/** Result of a detection scan — drives the button's background colour. */
enum class DetectionStatus(@ColorRes val colorRes: Int) {
    UNKNOWN(R.color.blocking_primary),
    SAFE(R.color.floating_safe),
    WARNING(R.color.floating_warning),
    DANGEROUS(R.color.floating_dangerous)
}

/** A single row item shown in the tap-to-expand action menu. */
data class FloatingMenuAction(
    @DrawableRes val iconRes: Int,
    val label: String,
    val onClick: () -> Unit
)
