package com.safeNest.demo.features.urlGuard.impl.urlGuard.util

import android.util.Log

/**
 * Tracks domains the user has explicitly allowed after seeing a blocking-page

 * ── Domain matching ──────────────────────────────────────────────────────────
 *   A domain matches an allowlist entry when:
 *     • it is identical to the stored entry, OR
 *     • it is a subdomain of the stored entry  (e.g. "sub.evil.com" matches "evil.com")
 *
 * ── Scope ────────────────────────────────────────────────────────────────────
 *   In-memory only. Call [clear] in [android.app.Service.onDestroy] so the
 *   warning reappears on the next service session
 */
class UserAllowedDomainGuard {

    private val allowedDomains = mutableSetOf<String>()

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns true if [domain] (or a parent domain) is in the allowlist,
     *
     * @param domain bare hostname, e.g. "evil.com" or "sub.evil.com"
     */
    fun isAllowed(domain: String): Boolean {
        val d = domain.trim()
        return allowedDomains.any { allowed -> d == allowed || d.endsWith(".$allowed") }
    }

    /**
     * Add [domain] to the allowlist.
     *
     * @param domain bare hostname extracted by [UrlExtractor.extractDomain]
     */
    fun allow(domain: String) {
        val d = domain.trim()
        if (allowedDomains.add(d)) {
            Log.d(TAG, "allowed: '$d'  (total: ${allowedDomains.size})")
        }
    }

    /**
     * Remove [domain] from the allowlist so the blocking page reappears on
     * the next visit. No-op if the domain was not present.
     */
    fun revoke(domain: String) {
        if (allowedDomains.remove(domain.trim())) {
            Log.d(TAG, "revoked: '${domain.trim()}'")
        }
    }

    /**
     * Wipe the entire allowlist.
     * Call this in [android.app.Service.onDestroy].
     */
    fun clear() {
        val count = allowedDomains.size
        allowedDomains.clear()
        Log.d(TAG, "cleared ($count domain(s) removed)")
    }

    /** Returns a snapshot of all currently allowed domains (for debugging). */
    fun snapshot(): Set<String> = allowedDomains.toSet()

    companion object {
        private const val TAG = "UserAllowedDomainGuard"
    }
}
