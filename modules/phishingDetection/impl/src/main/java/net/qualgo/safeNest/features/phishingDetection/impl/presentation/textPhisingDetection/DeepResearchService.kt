package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ApiResult
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.UrlCheckerResponse

data class DeepResearchResult(
    val scamPhoneCount: Int,
    val scamDomainCount: Int,
    val urlCheckerResponses: List<UrlCheckerResponse>,
)

object DeepResearchService {

    private const val TAG = "DeepResearchService"

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

    private suspend fun fetchUrlCheckerResults(urls: List<String>): List<UrlCheckerResponse> {
        return urls.mapNotNull { url ->
            when (val result = UrlCheckerApiClient.analyze(url)) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    Log.w(TAG, "URL check error for $url: ${result.error.detail}")
                    null
                }
                is ApiResult.Exception -> {
                    Log.e(TAG, "URL check exception for $url", result.throwable)
                    null
                }
            }
        }
    }
}
