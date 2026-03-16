package com.safeNest.demo.features.scamAnalyzer.impl.data.utils

import com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor.ExtractedEntities
import com.safeNest.demo.features.scamAnalyzer.impl.domain.models.UrlCheckerResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DeepResearchResult(
    val phoneMap: Map<String, String>,
    val domainMap: Map<String, String>,
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
        val phoneDeferred = async { checkPhones(entities.phoneNumbers) }
        val domainDeferred = async { checkDomains(entities.domains) }
        val urlDeferred = async { fetchUrlCheckerResults(entities.urls) }

        val phoneMap = phoneDeferred.await()
        val domainMap = domainDeferred.await()
        val urlCheckerResponses = urlDeferred.await()

        DeepResearchResult(
            phoneMap = phoneMap,
            domainMap = domainMap,
            urlCheckerResponses = urlCheckerResponses,
        )
    }

    private fun checkPhones(phones: List<String>): Map<String, String> {
        return phones.mapIndexed { idx, phone ->
            "PHONE_$idx" to if (phone in SCAM_PHONES)
                "Found in phone scam database"
            else
                "Not found in phone scam database"
        }.toMap()
    }

    private fun checkDomains(domains: List<String>): Map<String, String> {
        return domains.mapIndexed { idx, phone ->
            "DOMAIN_$idx" to if (phone in SCAM_DOMAINS)
                "Found in domain scam database"
            else
                "Not found in domain scam database"
        }.toMap()
    }

    private suspend fun fetchUrlCheckerResults(urls: List<String>): List<UrlCheckerResponse> {
//        return urls.mapNotNull { url ->
//            when (val result = UrlCheckerApiClient.analyze(url)) {
//                is ApiResult.Success -> result.data
//                is ApiResult.Error -> {
//                    Log.w(TAG, "URL check error for $url: ${result.error.detail}")
//                    null
//                }
//                is ApiResult.Exception -> {
//                    Log.e(TAG, "URL check exception for $url", result.throwable)
//                    null
//                }
//            }
//        }
        // call api to url checker
        return emptyList()
    }
}