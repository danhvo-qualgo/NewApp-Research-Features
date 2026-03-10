package net.qualgo.safeNest.features.phishingDetection.impl.presentation.models

data class UrlCheckerResponse(
    val url: String,
    val domain: String,
    val ssl: SslInfo,
    val homograph: HomographInfo,
    val typosquatting: TyposquattingInfo,
    val brandImpersonation: BrandImpersonationInfo,
    val pageContent: PageContentInfo,
    val heuristics: HeuristicsInfo,
)

data class SslInfo(
    val sslValid: Boolean,
    val error: String,
)

data class HomographInfo(
    val isHomograph: Boolean,
    val punycodeDomain: String,
    val decodedDomain: String,
)

data class TyposquattingInfo(
    val isTyposquatting: Boolean,
    val matchedBrand: String,
    val similarityScore: Double,
)

data class BrandImpersonationInfo(
    val isImpersonation: Boolean,
    val matchedBrand: String,
    val detectionMethod: String,
)

data class PageContentInfo(
    val reachable: Boolean,
    val title: String,
    val description: String,
    val redirected: Boolean,
    val finalUrl: String,
    val redirectChain: List<String>,
)

data class HeuristicsInfo(
    val suspiciousTld: Boolean,
    val tld: String,
    val suspiciousKeywords: List<String>,
    val paymentContextDetected: Boolean,
)
