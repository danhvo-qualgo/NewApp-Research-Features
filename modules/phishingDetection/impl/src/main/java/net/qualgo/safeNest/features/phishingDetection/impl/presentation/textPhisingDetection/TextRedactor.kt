package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities

object TextRedactor {

    fun redact(text: String, entities: ExtractedEntities): String {
        var result = text

        // Replace URLs first (before domains, since URLs contain domains)
        entities.urls.sortedByDescending { it.length }.forEach { url ->
            result = result.replace(url, "[URL]")
        }

        // Replace emails before domains (emails contain domain parts)
        entities.emails.sortedByDescending { it.length }.forEach { email ->
            result = result.replace(email, "[EMAIL]")
        }

        // Replace domains
        entities.domains.sortedByDescending { it.length }.forEach { domain ->
            result = result.replace(domain, "[DOMAIN]", ignoreCase = true)
        }

        // Replace phone numbers
        entities.phones.sortedByDescending { it.length }.forEach { phone ->
            // Match both the formatted form and possible raw digits-only variants
            result = result.replace(phone, "[PHONE_NUM]")
            val digitsOnly = phone.filter { it.isDigit() }
            if (digitsOnly.isNotEmpty()) {
                result = result.replace(digitsOnly, "[PHONE_NUM]")
            }
        }

        return result
    }
}
