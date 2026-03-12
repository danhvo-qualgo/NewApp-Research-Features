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
                appendLine(response.toSignalSummary())
            }
        }
    }.trimEnd()

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

    fun buildSmsSignalSummary(text: String): String {
        val signals = mutableListOf<String>()
        val lower = text.lowercase()

        val urgencyWords = listOf(
            "khẩn cấp", "ngay lập tức", "nhanh chóng", "hết hạn", "trúng thưởng", "miễn phí",
            "nhận ngay", "click", "xác nhận", "đăng nhập", "mật khẩu", "tài khoản bị khóa"
        )
        urgencyWords.firstOrNull { lower.contains(it.lowercase()) }?.let { matched ->
            signals += "urgency_keyword: \"$matched\""
        }

        if (text.contains("http://") || text.contains("https://")) {
            signals += "contains_url: true"
        }

        if (text.isNotEmpty() && (text.contains("0") || text.contains("+84"))) {
            signals += "possible_phone: true"
        }

        return if (signals.isEmpty()) "(no signals detected)" else signals.joinToString("\n")
    }
}
