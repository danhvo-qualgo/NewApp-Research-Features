package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import com.safeNest.demo.features.permissionManager.api.domain.GetAppPermissionInfoUseCase
import com.safeNest.demo.features.permissionManager.api.domain.model.PermissionProtectionLevel
import javax.inject.Inject

/**
 * Evaluates the trustworthiness of an installed app and maps the result to a [DetectionStatus].
 *
 * Base on there permission have been granted
 * Results are stored in [cache] so the caller can skip re-evaluation on subsequent visits.
 */
class AppTrustChecker @Inject constructor(
    private val context: Context,
    private val getAppPermissionInfoUseCase: GetAppPermissionInfoUseCase) {

    /** Session-scoped cache: package name → last evaluated [DetectionStatus]. */
    val cache = mutableMapOf<String, DetectionStatus>()

    /**
     * Evaluates [pkg] and returns a [DetectionStatus].
     * Must be called from a coroutine (performs blocking PackageManager calls on Default dispatcher).
     */
    suspend fun evaluate(pkg: String): DetectionStatus {
//        val installSrc  = withContext(Dispatchers.Default) { getInstallSource(pkg) }
//        val isPlayStore = installSrc == "com.android.vending"
//        val isDangerous = DANGEROUS_PACKAGES.contains(pkg)
//        val isKnownSafe = TRUSTED_PACKAGES.contains(pkg)
        if(pkg in TRUSTED_PACKAGES) return DetectionStatus.SAFE
        val appPermission = getAppPermissionInfoUseCase.invoke(pkg)
        Log.d(TAG, "App package [$pkg] → permission: $appPermission")
        val containSensitivePermission = appPermission.any { it.protectionLevel == PermissionProtectionLevel.DANGEROUS}
        val status = if (containSensitivePermission) {
            DetectionStatus.WARNING
        } else {
            DetectionStatus.SAFE
        }
        cache[pkg] = status

        Log.d(TAG, "AppTrust [$pkg]  → status: $status ")
        return status
    }

    /** Returns true if [pkg] is a system app (skips evaluation in the service). */
    fun isSystemApp(pkg: String): Boolean = try {
        (context.packageManager.getApplicationInfo(pkg, 0).flags and ApplicationInfo.FLAG_SYSTEM) != 0
    } catch (_: Exception) { false }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun getInstallSource(pkg: String): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(pkg).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(pkg)
        }
    } catch (_: Exception) { null }

    private enum class TrustLevel {
        TRUSTED, VERIFIED, UNKNOWN, DANGEROUS
    }

    // ── Known package lists ───────────────────────────────────────────────────

    companion object {
        private const val TAG = "AppTrustChecker"

        /** Browser packages — accessibility events from these trigger URL scanning. */
        val BROWSER_PACKAGES: Set<String> = setOf(
            // Chromium / Google
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            // Firefox
            "org.mozilla.firefox",
            "org.mozilla.fenix",
            // Opera
            "com.opera.browser",
            "com.opera.browser.beta",
            "com.opera.mini.native",
            "com.opera.mini.native.beta",
            // Microsoft Edge
            "com.microsoft.emmx",
            // Cốc Cốc
            "com.coccoc.trinhduyet",
            // UC Browser
            "com.UCMobile.intl",
            "com.uc.browser.en"
        )

        /** Social network apps — floating button is shown when these are in the foreground. */
        val SOCIAL_NETWORK_PACKAGES: Set<String> = setOf(
            // Facebook
            "com.facebook.katana",
            // Instagram
            "com.instagram.android",
            // Twitter / X
            "com.twitter.android",
            "com.x.android",
            // TikTok
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            // LinkedIn
            "com.linkedin.android",
            // Pinterest
            "com.pinterest",
            // Snapchat
            "com.snapchat.android",
            // YouTube
            "com.google.android.youtube"
        )

        /** OTT / messaging apps — floating button is shown when these are in the foreground. */
        val OTT_PACKAGES: Set<String> = setOf(
            // Zalo
            "com.zing.zalo",
            // Telegram
            "com.telegram.messenger",
            "org.telegram.messenger",
            // Messenger
            "com.facebook.orca",
            // WhatsApp
            "com.whatsapp",
            "com.whatsapp.w4b",
            // Viber
            "com.viber.voip",
            // Line
            "jp.naver.line.android",
            // Signal
            "org.thoughtcrime.securesms",
            // Skype
            "com.skype.raider",
            // Microsoft Teams
            "com.microsoft.teams"
        )

        /** SMS / MMS apps — notifications from these are scanned for scam content. */
        val SMS_PACKAGES: Set<String> = setOf(
            // AOSP Messages
            "com.android.mms",
            // Google Messages
            "com.google.android.apps.messaging",
            // Samsung Messages
            "com.samsung.android.messaging",
            // MIUI Messages
            "com.android.mms.miui",
            // Huawei Messaging
            "com.huawei.message",
            // OPPO Messages
            "com.coloros.mms",
            "com.oppo.mms",
            // Vivo Messages
            "com.vivo.mms",
            // OnePlus Messages
            "com.oneplus.mms"
        )

        /** Known in-call UI packages across major OEMs. */
        val CALL_PACKAGES: Set<String> = setOf(
            "com.android.incallui",
            "com.google.android.dialer",
            "com.samsung.android.incallui",
            "com.miui.incallui",
            "com.huawei.incallui",
            "com.oneplus.incallui",
            "com.coloros.incallui",
            "com.vivo.incallui"
        )

        /**
         * Union of all app categories whose notifications should be scanned for scam/phishing
         * content. Only notifications originating from a package in this set will be processed
         * by [onNotificationPosted]; all others are silently ignored.
         */
        val NOTIFICATION_SCAN_PACKAGES: Set<String> =
            OTT_PACKAGES + SOCIAL_NETWORK_PACKAGES + SMS_PACKAGES

        /** Well-known apps always treated as trusted regardless of install source. */
        val TRUSTED_PACKAGES: Set<String> = setOf(
            "com.vnptit.vneid",
            "vn.gdt.etaxmobile"
        )

        /** Packages known to be malicious or high-risk. Extend with threat-intel data. */
        val DANGEROUS_PACKAGES: Set<String> = setOf()
    }
}
