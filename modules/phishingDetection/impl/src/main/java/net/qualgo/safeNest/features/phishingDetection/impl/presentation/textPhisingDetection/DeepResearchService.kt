package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ConfusableChar
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.HomographInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.PageInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.SslInfo
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.TyposquatInfo
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
        val isHttps = url.startsWith("https://")

        return UrlCheckerResponse(
            url = url,
            ssl = SslInfo(
                valid = isHttps,
                issuer = if (isHttps) "" else "",
                protocol = if (isHttps) "TLSv1.3" else "",
                expiresAt = "",
                daysUntilExpiry = 0,
                subjectAltNames = emptyList(),
            ),
            homograph = HomographInfo(
                isHomograph = false,
                isIDN = false,
                hasMixedScripts = false,
                punycode = "",
                score = 0.0,
                confusableChars = emptyList(),
            ),
            typosquat = TyposquatInfo(
                isTyposquat = false,
                matchedDomain = "",
                distance = 0,
                score = 0.0,
            ),
            pageInfo = PageInfo(
                reachable = true,
                statusCode = 200,
                title = "",
                description = "",
                finalUrl = url,
            ),
        )
    }
}
