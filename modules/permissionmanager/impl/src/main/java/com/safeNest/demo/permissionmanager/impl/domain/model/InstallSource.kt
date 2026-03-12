package com.safeNest.demo.permissionmanager.impl.domain.model

/**
 * Represents where an application was installed from.
 */
sealed class InstallSource {

    /** Installed from a well-known app store (e.g. Google Play). */
    data class KnownStore(
        /** Human-readable store name, e.g. "Google Play Store". */
        val storeName: String,
        /** The installer's package name, e.g. "com.android.vending". */
        val installerPackage: String
    ) : InstallSource()

    /**
     * Installed manually via an APK file (sideloaded).
     * The raw installer package name is kept for diagnostics.
     */
    data class Sideloaded(val installerPackage: String?) : InstallSource()

    /** Pre-installed with the device / ROM — no installer recorded. */
    object Preinstalled : InstallSource()

    /** Installer information is not available or could not be determined. */
    object Unknown : InstallSource()

    // ──────────────────────────────────────────────────────────────────────────
    // Companion helpers
    // ──────────────────────────────────────────────────────────────────────────

    companion object {
        /** Maps well-known installer package names to human-friendly store names. */
        private val KNOWN_STORES: Map<String, String> = mapOf(
            "com.android.vending"                   to "Google Play Store",
            "com.google.android.feedback"           to "Google Play Store",
            "com.amazon.venezia"                    to "Amazon Appstore",
            "com.sec.android.app.samsungapps"       to "Samsung Galaxy Store",
            "com.huawei.appmarket"                  to "Huawei AppGallery",
            "com.xiaomi.market"                     to "Xiaomi GetApps",
            "com.oppo.market"                       to "OPPO App Market",
            "com.vivo.appstore"                     to "vivo App Store",
            "com.oneplus.store"                     to "OnePlus Store",
            "com.lge.lgsmartworld"                  to "LG SmartWorld",
            "com.yandex.store"                      to "Yandex Store",
            "com.aptoide.partners"                  to "Aptoide",
            "cm.aptoide.pt"                         to "Aptoide",
            "com.getjar.reward"                     to "GetJar",
            "org.fdroid.fdroid"                     to "F-Droid",
            "com.aurora.store"                      to "Aurora Store"
        )

        /** Package names that indicate a manual APK install. */
        private val SIDELOAD_INSTALLERS: Set<String> = setOf(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.miui.packageinstaller",
            "com.samsung.android.packageinstaller",
            "com.huawei.systemmanager",
            "com.sec.android.preloadinstaller"
        )

        /**
         * Resolves an [InstallSource] from a raw [installerPackageName].
         *
         * @param installerPackageName The value returned by
         *   [PackageManager.getInstallerPackageName] / [InstallSourceInfo].
         *   `null` means either preinstalled or unknown.
         * @param isSystemApp Whether the target app is a system app — used to
         *   distinguish [Preinstalled] from [Unknown] when no installer is recorded.
         */
        fun from(installerPackageName: String?, isSystemApp: Boolean): InstallSource {
            if (installerPackageName == null) {
                return if (isSystemApp) Preinstalled else Unknown
            }
            KNOWN_STORES[installerPackageName]?.let { storeName ->
                return KnownStore(storeName, installerPackageName)
            }
            if (installerPackageName in SIDELOAD_INSTALLERS) {
                return Sideloaded(installerPackageName)
            }
            // Unrecognised installer — treat as sideloaded and surface the raw package name
            return Sideloaded(installerPackageName)
        }
    }
}
