package com.example.impl.urlguard

import android.util.Log
import java.net.URL
import java.util.regex.Pattern

/**
 * Checks URLs against a threat source. Replace with your real engine (e.g. Safe Browsing API).
 */
interface ThreatEngine {
    suspend fun isMalicious(url: String): Boolean
}



/**
 * Example implementation: blocklist + optional domain blocklist.
 * In production, use Google Safe Browsing API or similar.
 */
class DefaultThreatEngine : ThreatEngine {

    override suspend fun isMalicious(url: String): Boolean {
        if (url.isBlank()) return false
        val normalized = normalizeUrl(url) ?: return false
        val blocked = BLOCKED_DOMAINS.any { normalized.contains(it) } ||
            BLOCKED_PATTERNS.any { Pattern.compile(it).matcher(normalized).find() }
        if (blocked) Log.w(TAG, "ThreatEngine: blocked URL $normalized")
        return blocked
    }

    private fun normalizeUrl(raw: String): String {
        return try {
            var s = raw.trim()
            if (!s.startsWith("http://") && !s.startsWith("https://")) s = "https://$s"
            URL(s).host?.lowercase() ?: raw
        } catch (_: Exception) {
            raw
        }
    }

    companion object {
        private const val TAG = "ThreatEngine"
        // Example blocklist — replace with your threat feed or API.
        private val BLOCKED_DOMAINS = setOf(
            "malware.example.com",
            "phishing.example.com",
            "evil-site.test"
        )
        private val BLOCKED_PATTERNS = listOf(
            "malware\\.example",
            "phishing\\.example"
        )
    }
}
