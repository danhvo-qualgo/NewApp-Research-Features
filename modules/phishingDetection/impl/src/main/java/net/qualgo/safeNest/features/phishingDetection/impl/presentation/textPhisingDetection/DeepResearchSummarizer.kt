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
            appendLine("  \"domain\": \"$domain\",")
            appendLine("  \"ssl\": {")
            appendLine("    \"ssl_valid\": ${ssl.sslValid},")
            appendLine("    \"error\": \"${ssl.error}\"")
            appendLine("  },")
            appendLine("  \"homograph\": {")
            appendLine("    \"is_homograph\": ${homograph.isHomograph},")
            appendLine("    \"punycode_domain\": \"${homograph.punycodeDomain}\",")
            appendLine("    \"decoded_domain\": \"${homograph.decodedDomain}\"")
            appendLine("  },")
            appendLine("  \"typosquatting\": {")
            appendLine("    \"is_typosquatting\": ${typosquatting.isTyposquatting},")
            appendLine("    \"matched_brand\": \"${typosquatting.matchedBrand}\",")
            appendLine("    \"similarity_score\": ${typosquatting.similarityScore}")
            appendLine("  },")
            appendLine("  \"brand_impersonation\": {")
            appendLine("    \"is_impersonation\": ${brandImpersonation.isImpersonation},")
            appendLine("    \"matched_brand\": \"${brandImpersonation.matchedBrand}\",")
            appendLine("    \"detection_method\": \"${brandImpersonation.detectionMethod}\"")
            appendLine("  },")
            appendLine("  \"page_content\": {")
            appendLine("    \"reachable\": ${pageContent.reachable},")
            appendLine("    \"title\": \"${pageContent.title}\",")
            appendLine("    \"description\": \"${pageContent.description}\",")
            appendLine("    \"redirected\": ${pageContent.redirected},")
            appendLine("    \"final_url\": \"${pageContent.finalUrl}\",")
            appendLine("    \"redirect_chain\": [${pageContent.redirectChain.joinToString { "\"$it\"" }}]")
            appendLine("  },")
            appendLine("  \"heuristics\": {")
            appendLine("    \"suspicious_tld\": ${heuristics.suspiciousTld},")
            appendLine("    \"tld\": \"${heuristics.tld}\",")
            appendLine("    \"suspicious_keywords\": [${heuristics.suspiciousKeywords.joinToString { "\"$it\"" }}],")
            appendLine("    \"payment_context_detected\": ${heuristics.paymentContextDetected}")
            append("  }")
            append("\n}")
        }
    }
}
