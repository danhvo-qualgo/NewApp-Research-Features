package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Stateless helper that extracts and normalises a URL from an accessibility tree.
 *
 * Lookup order:
 *   1. Known view ID for the active browser package (fast, zero traversal)
 *   2. Depth-first text traversal as a fallback for unknown/updated browsers
 */
object UrlExtractor {

    /**
     * Known address-bar view resource IDs keyed by browser package name.
     * Used both for URL extraction and address-bar focus detection.
     */
    val ADDRESS_BAR_VIEW_IDS: Map<String, String> = mapOf(
        // Chrome family
        "com.android.chrome"           to "com.android.chrome:id/url_bar",
        "com.chrome.beta"              to "com.chrome.beta:id/url_bar",
        "com.chrome.dev"               to "com.chrome.dev:id/url_bar",
        "com.chrome.canary"            to "com.chrome.canary:id/url_bar",
        // Firefox / Fenix
        "org.mozilla.firefox"          to "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
        "org.mozilla.firefox_beta"     to "org.mozilla.firefox_beta:id/mozac_browser_toolbar_url_view",
        "org.mozilla.fenix"            to "org.mozilla.fenix:id/mozac_browser_toolbar_url_view",
        // Brave
        "com.brave.browser"            to "com.brave.browser:id/url_bar",
        // Microsoft Edge
        "com.microsoft.emmx"           to "com.microsoft.emmx:id/url_bar",
        // Opera
        "com.opera.browser"            to "com.opera.browser:id/url_field",
        "com.opera.browser.beta"       to "com.opera.browser.beta:id/url_field",
        "com.opera.mini.native"        to "com.opera.mini.native:id/url_field",
        "com.opera.mini.native.beta"   to "com.opera.mini.native.beta:id/url_field",
        // Samsung Internet
        "com.sec.android.app.sbrowser" to "com.sec.android.app.sbrowser:id/location_bar_edit_text",
        // Kiwi / Vivaldi
        "com.kiwibrowser.browser"      to "com.kiwibrowser.browser:id/url_bar",
        "com.vivaldi.browser"          to "com.vivaldi.browser:id/url_bar",
        // Cốc Cốc (Chromium-based)
        "com.coccoc.trinhduyet"        to "com.coccoc.trinhduyet:id/url_bar",
        // UC Browser
        "com.UCMobile.intl"            to "com.UCMobile.intl:id/url_bar",
        "com.uc.browser.en"            to "com.uc.browser.en:id/url_bar",
    )

    /**
     * Returns the best-guess URL visible in the active browser window,
     * or null if none could be found.
     *
     * @param root       Root [AccessibilityNodeInfo] of the active window.
     *                   Recycled before this function returns — do not use afterwards.
     * @param browserPkg Package name of the active browser. When provided, the known
     *                   view ID for that package is tried first before falling back to
     *                   a full tree traversal.
     */
    fun extract(root: AccessibilityNodeInfo?, browserPkg: String? = null): String? {
        if (root == null) return null
        return try {
            extractByViewId(root, browserPkg) ?: extractByTraversing(root)
        } finally {
            root.recycle()
        }
    }

    /**
     * Prepends "https://" to bare hostnames / paths so downstream callers
     * always receive a properly-formed URI.
     */
    fun normalize(raw: String): String = try {
        var s = raw.trim()
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "https://$s"
        s
    } catch (_: Exception) { raw }

    /**
     * Extracts the bare hostname from [url], stripping "www." and lowercasing.
     * Returns null if the host cannot be determined.
     */
    fun extractDomain(url: String): String? = try {
        val normalized = normalize(url)
        java.net.URI(normalized).host
            ?.trim()
            ?.lowercase()
            ?.removePrefix("www.")
            ?.takeIf { it.isNotBlank() }
    } catch (_: Exception) { null }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Tries to extract a URL using the known view ID for [browserPkg].
     * Returns null if no mapping exists, the node is not found, or the
     * text does not look like a URL.
     */
    private fun extractByViewId(root: AccessibilityNodeInfo, browserPkg: String?): String? {
        val viewId = browserPkg?.let { ADDRESS_BAR_VIEW_IDS[it] } ?: return null
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        val text  = nodes.firstOrNull()?.text?.toString()?.trim()
        nodes.forEach { it.recycle() }
        return text?.takeIf { it.isNotBlank() && looksLikeUrl(it) }
    }

    private fun extractByTraversing(node: AccessibilityNodeInfo): String? {
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank() && looksLikeUrl(text)) return text
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = extractByTraversing(child)
            child.recycle()
            if (found != null) return found
        }
        return null
    }

    /**
     * Returns true only if [s] looks like an actual URL or bare hostname.
     */
    private fun looksLikeUrl(s: String): Boolean {
        if (s.isBlank() || s.length > 2_048) return false
        if (s.contains(' ') || s.contains('\n')) return false
        return s.startsWith("http://") ||
               s.startsWith("https://") ||
               BARE_HOST_PATTERN.containsMatchIn(s)
    }

    private val BARE_HOST_PATTERN = Regex("""^[a-zA-Z0-9]([a-zA-Z0-9\-]*\.)+[a-zA-Z]{2,}""")
}
