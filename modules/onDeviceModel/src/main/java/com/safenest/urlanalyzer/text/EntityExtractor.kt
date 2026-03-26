/*
 * EntityExtractor.kt — Regex-based entity extraction from text.
 * Extracts phone numbers, URLs/domains, and email addresses.
 */
package com.safenest.urlanalyzer.text

data class ExtractedEntities(
    val phones: List<String>,
    val urls: List<String>,
    val emails: List<String>
)

object EntityExtractor {

    private val PHONE_REGEX = Regex(
        """(?:\+?\d{1,3}[-.\s]?)?\(?\d{2,4}\)?[-.\s]?\d{3,4}[-.\s]?\d{3,4}"""
    )

    private val URL_REGEX = Regex(
        """(?:https?://)?(?:[\w-]+\.)+[a-zA-Z]{2,}(?:/[^\s]*)?""",
        RegexOption.IGNORE_CASE
    )

    private val EMAIL_REGEX = Regex(
        """[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""",
        RegexOption.IGNORE_CASE
    )

    fun extract(text: String): ExtractedEntities {
        val emails = EMAIL_REGEX.findAll(text).map { it.value }.toList()
        val emailDomains = emails.map { it.substringAfter("@") }.toSet()

        val urls = URL_REGEX.findAll(text)
            .map { it.value }
            .filter { url ->
                // Exclude URLs that are just email domains
                !emailDomains.any { domain -> url == domain || url.endsWith(".$domain") }
            }
            .toList()

        val phones = PHONE_REGEX.findAll(text)
            .map { it.value.trim() }
            .filter { it.replace(Regex("[^\\d]"), "").length >= 8 }
            .toList()

        return ExtractedEntities(phones = phones, urls = urls, emails = emails)
    }
}
