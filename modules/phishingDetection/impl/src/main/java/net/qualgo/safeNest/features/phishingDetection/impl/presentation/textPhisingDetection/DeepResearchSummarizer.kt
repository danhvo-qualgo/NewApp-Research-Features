package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.UrlCheckerResponse

object DeepResearchSummarizer {

    fun summarize(text: String, result: DeepResearchResult): String {
        val urlMap = result.urlCheckerResponses.mapIndexed { idx, response ->
            "URL_$idx" to response.toSignalSummary()
        }.toMap()
        val combinedMap = urlMap + result.phoneMap + result.domainMap
        return buildSmsSignalSummary(text, combinedMap)
    }

    private fun UrlCheckerResponse.toSignalSummary(): String = buildString {
        appendLine(
            "homograph: is_homograph=${homograph.isHomograph}, score=${"%.3f".format(homograph.score)}"
        )
        appendLine(
            "typosquat: is_typosquat=${typosquat.isTyposquat}, matched_domain=${typosquat.matchedDomain}, score=${"%.3f".format(typosquat.score)}"
        )
        appendLine(
            "ssl: valid=${ssl.valid}, issuer=${ssl.issuer}, days_until_expiry=${ssl.daysUntilExpiry}"
        )
        appendLine(
            "page_info: reachable=${pageInfo.reachable}, final_url=${pageInfo.finalUrl}, title=${pageInfo.title}"
        )
    }.trimEnd()

    fun buildSmsSignalSummary(text: String, entities: Map<String, String>): String {
        val signals = mutableListOf<String>()

        for ((placeholder, value) in entities) {
            when {
                placeholder.startsWith("URL_") -> signals += "extracted_url $placeholder: $value"
                placeholder.startsWith("EMAIL_") -> signals += "extracted_email $placeholder: $value"
                placeholder.startsWith("PHONE_") -> signals += "extracted_phone $placeholder: $value"
                placeholder.startsWith("DOMAIN_") -> signals += "extracted_domain $placeholder: $value"
            }
        }

        val urgencyWords = listOf(
            "khẩn cấp", "ngay lập tức", "nhanh chóng", "hết hạn", "trúng thưởng", "miễn phí",
            "nhận ngay", "click", "xác nhận", "đăng nhập", "mật khẩu", "tài khoản bị khóa"
        )
        urgencyWords.firstOrNull { text.lowercase().contains(it.lowercase()) }?.let { matched ->
            signals += "urgency_keyword: \"$matched\""
        }

        return if (signals.isEmpty()) "(no signals detected)" else signals.joinToString("\n")
    }
}
