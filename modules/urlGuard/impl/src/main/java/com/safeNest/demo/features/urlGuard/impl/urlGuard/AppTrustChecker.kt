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
            "com.android.chrome",
            "org.mozilla.firefox",
            "org.mozilla.fenix",
            "com.opera.browser",
            "com.microsoft.emmx"
        )

        /** Well-known apps always treated as trusted regardless of install source. */
        val TRUSTED_PACKAGES: Set<String> = setOf(
            // Google
            "com.google.android.gm",
            "com.google.android.apps.maps",
            "com.google.android.youtube",
            "com.google.android.apps.docs",
            "com.google.android.keep",
            // Social / messaging
            "com.whatsapp",
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.telegram.messenger",
            "org.telegram.messenger",
            // Entertainment
            "com.netflix.mediaclient",
            "com.spotify.music",
            // Shopping / finance
            "com.amazon.mShop.android.shopping",
            "com.paypal.android.p2pmobile",
            // Vietnamese apps
            "vn.momo.party",
            "com.vnpay.hdbank",
            "com.zalopay.wallet",
            "vn.tiki.app.tikishopping"
        )

        /** Packages known to be malicious or high-risk. Extend with threat-intel data. */
        val DANGEROUS_PACKAGES: Set<String> = setOf()

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
    }
}
