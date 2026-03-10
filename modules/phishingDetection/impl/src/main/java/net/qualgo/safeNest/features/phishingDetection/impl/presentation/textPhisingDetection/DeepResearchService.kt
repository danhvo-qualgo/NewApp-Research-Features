package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.BrandImpersonationInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.HeuristicsInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.HomographInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.PageContentInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.SslInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.TyposquattingInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.UrlCheckerResponse

data class DeepResearchResult(
    val scamPhoneCount: Int,
    val scamDomainCount: Int,
    val urlCheckerResponses: List<UrlCheckerResponse>,
)

object DeepResearchService {

    private val SCAM_PHONES = setOf(
        "0814936074",
        "0906279817",
        "0924442145",
        "0846470323",
        "0789632517",
        "0775280845",
    )

    private val SCAM_DOMAINS = setOf(
        "oshopee.net",
        "oshopee.cc",
        "oshopee.vip",
        "hopee88.vip",
        "hopeevp.com",
        "hopee585.com",
        "iki8.vip",
        "hopeeft.com",
    )

    suspend fun research(entities: ExtractedEntities): DeepResearchResult = coroutineScope {
        val phoneDeferred = async { checkPhones(entities.phones) }
        val domainDeferred = async { checkDomains(entities.domains) }
        val urlDeferred = async { fetchUrlCheckerResults(entities.urls) }

        val scamPhoneCount = phoneDeferred.await()
        val scamDomainCount = domainDeferred.await()
        val urlCheckerResponses = urlDeferred.await()

        DeepResearchResult(
            scamPhoneCount = scamPhoneCount,
            scamDomainCount = scamDomainCount,
            urlCheckerResponses = urlCheckerResponses,
        )
    }

    private fun checkPhones(phones: List<String>): Int {
        return phones.count { phone ->
            val digitsOnly = phone.filter { it.isDigit() }
            SCAM_PHONES.any { scam -> digitsOnly.endsWith(scam.filter { it.isDigit() }) }
        }
    }

    private fun checkDomains(domains: List<String>): Int {
        return domains.count { domain ->
            SCAM_DOMAINS.contains(domain.lowercase().removePrefix("www."))
        }
    }

    private fun fetchUrlCheckerResults(urls: List<String>): List<UrlCheckerResponse> {
        return urls.map { url -> mockUrlCheckerResponse(url) }
    }

    private fun mockUrlCheckerResponse(url: String): UrlCheckerResponse {
        val domain = runCatching {
            java.net.URL(url).host.removePrefix("www.")
        }.getOrDefault("")

        return UrlCheckerResponse(
            url = url,
            domain = domain,
            ssl = SslInfo(
                sslValid = url.startsWith("https://"),
                error = if (url.startsWith("https://")) "" else "No SSL certificate",
            ),
            homograph = HomographInfo(
                isHomograph = false,
                punycodeDomain = domain,
                decodedDomain = domain,
            ),
            typosquatting = TyposquattingInfo(
                isTyposquatting = SCAM_DOMAINS.any { scam ->
                    domain.contains(scam.substringBefore(".")) && domain != scam
                },
                matchedBrand = "",
                similarityScore = 0.0,
            ),
            brandImpersonation = BrandImpersonationInfo(
                isImpersonation = false,
                matchedBrand = "",
                detectionMethod = "",
            ),
            pageContent = PageContentInfo(
                reachable = true,
                title = "",
                description = "",
                redirected = false,
                finalUrl = url,
                redirectChain = emptyList(),
            ),
            heuristics = HeuristicsInfo(
                suspiciousTld = listOf(".vip", ".cc", ".xyz", ".top", ".tk").any { tld ->
                    domain.endsWith(tld)
                },
                tld = domain.substringAfterLast(".").let { ".$it" },
                suspiciousKeywords = emptyList(),
                paymentContextDetected = false,
            ),
        )
    }
}
