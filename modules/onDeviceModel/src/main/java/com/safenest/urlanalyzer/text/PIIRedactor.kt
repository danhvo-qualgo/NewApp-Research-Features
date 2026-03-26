/*
 * PIIRedactor.kt — Redact PII from text before LLM analysis.
 * Replaces phones with [REDACTED PHONE], emails with [REDACTED EMAIL @domain.com],
 * and URLs with [URL: domain.tld] (strips query params).
 */
package com.safenest.urlanalyzer.text

import java.net.URI

object PIIRedactor {

    fun redact(text: String, entities: ExtractedEntities): String {
        var result = text

        // Redact emails — keep domain part
        for (email in entities.emails) {
            val domain = email.substringAfter("@")
            result = result.replace(email, "[REDACTED EMAIL @$domain]")
        }

        // Redact URLs — keep eTLD+1, strip path and query params
        for (url in entities.urls) {
            val cleanDomain = extractDomain(url)
            result = result.replace(url, "[URL: $cleanDomain]")
        }

        // Redact phones
        for (phone in entities.phones) {
            result = result.replace(phone, "[REDACTED PHONE]")
        }

        return result
    }

    private fun extractDomain(url: String): String {
        return try {
            val withScheme = if (url.startsWith("http")) url else "https://$url"
            val uri = URI(withScheme)
            uri.host ?: url
        } catch (e: Exception) {
            url.substringBefore("/").substringBefore("?")
        }
    }
}
