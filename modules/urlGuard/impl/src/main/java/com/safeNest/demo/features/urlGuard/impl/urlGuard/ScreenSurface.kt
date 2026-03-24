package com.safeNest.demo.features.urlGuard.impl.urlGuard

import com.safeNest.demo.features.commonKotlin.IncomingCallType

/**
 * Represents what is currently visible on the user's screen,
 * as detected by [UrlGuardAccessibilityService].
 *
 * Each variant carries the minimal data needed to drive [SecureView]
 * (button icon + status colour).
 *
 *  ┌────────────────────────────────────────────────────────────────────┐
 *  │  Surface          │  FloatingButtonFeature  │  default Status      │
 *  ├────────────────────────────────────────────────────────────────────┤
 *  │  Idle             │  DEFAULT                │  UNKNOWN             │
 *  │  Browser          │  SAFE_BROWSING          │  UNKNOWN → scanned   │
 *  │  ActiveCall       │  CALL_PROTECTION        │  UNKNOWN             │
 *  │  Notification     │  SMS_CHECK              │  UNKNOWN             │
 *  │  App              │  DEFAULT                │  UNKNOWN → trusted   │
 *  └────────────────────────────────────────────────────────────────────┘
 */
sealed class ScreenSurface {

    // ── No relevant app in foreground (launcher, system UI, etc.) ────────────
    object Idle : ScreenSurface()

    // ── User is browsing the web ──────────────────────────────────────────────
    data class Browser(
        val packageName: String,
        val url: String?,
        val status: DetectionStatus = DetectionStatus.UNKNOWN
    ) : ScreenSurface()

    // ── Active or incoming phone call ─────────────────────────────────────────
    data class ActiveCall(
        val phoneNumber: String?,
        val status: DetectionStatus = DetectionStatus.UNKNOWN,
        val fromEvent: IncomingCallType? = null
    ) : ScreenSurface()

    // ── A status-bar notification was posted ──────────────────────────────────
    data class Notification(
        /** Package that posted the notification. */
        val packageName: String,
        val title: String?,
        val content: String?,
        val status: DetectionStatus = DetectionStatus.UNKNOWN
    ) : ScreenSurface()

    // ── Any other user-installed, non-system app ──────────────────────────────
    data class App(
        val packageName: String,
        /** Result of the app-trust evaluation. Starts as UNKNOWN. */
        val status: DetectionStatus = DetectionStatus.UNKNOWN
    ) : ScreenSurface()
}
