package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.UrlCheckerResponse

object DeepResearchSummarizer {

    fun summarize(result: DeepResearchResult): String = buildString {
        if (result.scamPhoneCount > 0) {
            appendLine("- ${result.scamPhoneCount} phone number(s) were found in phone scam database")
        }
        if (result.scamDomainCount > 0) {
            appendLine("- ${result.scamDomainCount} domain(s) were found in domain scam database")
        }
        if (result.urlCheckerResponses.isNotEmpty()) {
            result.urlCheckerResponses.forEach { response ->
                appendLine("- The analysis result of URL Checker for ${response.url}:")
                appendLine(response.toSummaryJson())
            }
        }
    }.trimEnd()

    private fun UrlCheckerResponse.toSummaryJson(): String {
        return buildString {
            appendLine("{")
            appendLine("  \"url\": \"$url\",")
            appendLine("  \"ssl\": {")
            appendLine("    \"valid\": ${ssl.valid},")
            appendLine("    \"issuer\": \"${ssl.issuer}\",")
            appendLine("    \"protocol\": \"${ssl.protocol}\",")
            appendLine("    \"expiresAt\": \"${ssl.expiresAt}\",")
            appendLine("    \"daysUntilExpiry\": ${ssl.daysUntilExpiry},")
            appendLine("    \"subjectAltNames\": [${ssl.subjectAltNames.joinToString { "\"$it\"" }}]")
            appendLine("  },")
            appendLine("  \"homograph\": {")
            appendLine("    \"isHomograph\": ${homograph.isHomograph},")
            appendLine("    \"isIDN\": ${homograph.isIDN},")
            appendLine("    \"hasMixedScripts\": ${homograph.hasMixedScripts},")
            appendLine("    \"punycode\": \"${homograph.punycode}\",")
            appendLine("    \"score\": ${homograph.score},")
            appendLine("    \"confusableChars\": [")
            homograph.confusableChars.forEachIndexed { i, c ->
                val comma = if (i < homograph.confusableChars.size - 1) "," else ""
                appendLine("      { \"char\": \"${c.char}\", \"looksLike\": \"${c.looksLike}\", \"script\": \"${c.script}\", \"unicode\": \"${c.unicode}\" }$comma")
            }
            appendLine("    ]")
            appendLine("  },")
            appendLine("  \"typosquat\": {")
            appendLine("    \"isTyposquat\": ${typosquat.isTyposquat},")
            appendLine("    \"matchedDomain\": \"${typosquat.matchedDomain}\",")
            appendLine("    \"distance\": ${typosquat.distance},")
            appendLine("    \"score\": ${typosquat.score}")
            appendLine("  },")
            appendLine("  \"pageInfo\": {")
            appendLine("    \"reachable\": ${pageInfo.reachable},")
            appendLine("    \"statusCode\": ${pageInfo.statusCode},")
            appendLine("    \"title\": \"${pageInfo.title}\",")
            appendLine("    \"description\": \"${pageInfo.description}\",")
            appendLine("    \"finalUrl\": \"${pageInfo.finalUrl}\"")
            append("  }")
            append("\n}")
        }
    }
}
