package com.safeNest.demo.features.urlGuard.impl.urlGuard

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Stateless helper that extracts and normalises a URL from an accessibility tree.
 *
 * Supports Chrome and Firefox/Fenix address bars out-of-the-box.
 * Falls back to a depth-first text traversal when view IDs are unavailable.
 */
object UrlExtractor {

    /**
     * Returns the best-guess URL visible in the active browser window,
     * or null if none could be found.
     * Recycles [root] before returning (caller must not use it afterwards).
     */
    fun extract(root: AccessibilityNodeInfo?): String? {
        if (root == null) return null
        return try {
            extractByViewId(root) ?: extractByTraversing(root)
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

    private fun extractByViewId(root: AccessibilityNodeInfo): String? {
        // Chrome
        val chromeNodes = root.findAccessibilityNodeInfosByViewId("com.android.chrome:id/url_bar")
        val chromeText  = chromeNodes.firstOrNull()?.text?.toString()?.trim()
        chromeNodes.forEach { it.recycle() }
        if (!chromeText.isNullOrBlank() && looksLikeUrl(chromeText)) return chromeText

        // Firefox / Fenix
        val ffNodes = root.findAccessibilityNodeInfosByViewId(
            "org.mozilla.fenix:id/mozac_browser_toolbar_url_view"
        )
        val ffText = ffNodes.firstOrNull()?.text?.toString()?.trim()
        ffNodes.forEach { it.recycle() }
        if (!ffText.isNullOrBlank() && looksLikeUrl(ffText)) return ffText

        return null
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
