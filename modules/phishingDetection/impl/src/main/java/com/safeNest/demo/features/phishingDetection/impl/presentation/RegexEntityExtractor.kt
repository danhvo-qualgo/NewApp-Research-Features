package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import com.google.i18n.phonenumbers.PhoneNumberUtil
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities

class RegexEntityExtractor {

    private val phoneUtil = PhoneNumberUtil.getInstance()

    private val emailRegex = Regex(
        """[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}"""
    )

    private val urlRegex = Regex(
        """https?://[^\s/$.?#].[^\s]*""",
        RegexOption.IGNORE_CASE
    )

    private val domainRegex = Regex(
        """(?<![/@\w])([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(?![.\w])"""
    )

    fun extract(text: String): ExtractedEntities {
        val emails = emailRegex.findAll(text).map { it.value }.distinct().toList()

        val urls = urlRegex.findAll(text).map { it.value.trimEnd('.', ',', ')') }.distinct().toList()

        val phones = extractPhones(text)

        val urlHosts = urls.mapNotNull { url ->
            runCatching {
                java.net.URL(url).host.removePrefix("www.")
            }.getOrNull()
        }.toSet()

        val domains = domainRegex.findAll(text)
            .map { it.value.lowercase().removePrefix("www.") }
            .filter { domain ->
                domain !in urlHosts &&
                    emails.none { email -> email.endsWith("@$domain") } &&
                    !domain.matches(Regex("""\d+\.\d+\.\d+\.\d+"""))
            }
            .distinct()
            .toList()

        return ExtractedEntities(
            phones = phones,
            emails = emails,
            urls = urls,
            domains = domains,
        )
    }

    private fun extractPhones(text: String): List<String> {
        val matches = phoneUtil.findNumbers(text, "US")
        return matches.map { match ->
            phoneUtil.format(match.number(), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        }.distinct()
    }
}
