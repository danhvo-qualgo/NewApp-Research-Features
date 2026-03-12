package com.safeNest.demo.permissionmanager.impl.domain.model

import android.content.pm.ApplicationInfo
import android.os.Build

/**
 * A type-safe representation of [ApplicationInfo.category] (API 26+).
 *
 * Each entry carries:
 *  - [label]  – human-readable display name
 *  - [emoji]  – single emoji used as a compact visual indicator in the UI
 */
enum class AppCategory(val label: String, val emoji: String) {
    GAME("Game", "🎮"),
    AUDIO("Audio", "🎵"),
    VIDEO("Video", "🎬"),
    IMAGE("Image", "🖼"),
    SOCIAL("Social", "💬"),
    NEWS("News & Magazines", "📰"),
    MAPS("Maps & Navigation", "🗺"),
    PRODUCTIVITY("Productivity", "💼"),
    ACCESSIBILITY("Accessibility", "♿"),
    UNDEFINED("Uncategorised", "📦");

    companion object {
        /**
         * Converts a raw [ApplicationInfo.category] integer value to [AppCategory].
         * Falls back to [UNDEFINED] for any unrecognised or pre-API-26 value.
         */
        fun fromRaw(raw: Int): AppCategory {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return UNDEFINED
            return when (raw) {
                ApplicationInfo.CATEGORY_GAME         -> GAME
                ApplicationInfo.CATEGORY_AUDIO        -> AUDIO
                ApplicationInfo.CATEGORY_VIDEO        -> VIDEO
                ApplicationInfo.CATEGORY_IMAGE        -> IMAGE
                ApplicationInfo.CATEGORY_SOCIAL       -> SOCIAL
                ApplicationInfo.CATEGORY_NEWS         -> NEWS
                ApplicationInfo.CATEGORY_MAPS         -> MAPS
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> PRODUCTIVITY
                // CATEGORY_ACCESSIBILITY added in API 31
                8 /* ApplicationInfo.CATEGORY_ACCESSIBILITY */ -> ACCESSIBILITY
                else -> UNDEFINED
            }
        }
    }
}
