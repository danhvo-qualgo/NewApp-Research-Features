package net.qualgo.safeNest.features.phishingDetection.impl.presentation.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UrlCheckerResponse(
    val url: String,
    val ssl: SslInfo,
    val homograph: HomographInfo,
    val typosquat: TyposquatInfo,
    val pageInfo: PageInfo,
)

@Serializable
data class SslInfo(
    val valid: Boolean,
    val issuer: String,
    val protocol: String,
    val expiresAt: String,
    val daysUntilExpiry: Int,
    val subjectAltNames: List<String>?,
)

@Serializable
data class ConfusableChar(
    val char: String,
    val looksLike: String,
    val script: String,
    val unicode: String,
)

@Serializable
data class HomographInfo(
    val isHomograph: Boolean,
    val isIDN: Boolean,
    val hasMixedScripts: Boolean,
    val punycode: String,
    val score: Double,
    val confusableChars: List<ConfusableChar>,
)

@Serializable
data class TyposquatInfo(
    val isTyposquat: Boolean,
    val matchedDomain: String,
    val distance: Int,
    val score: Double,
)

@Serializable
data class PageInfo(
    val reachable: Boolean,
    val statusCode: Int,
    val title: String,
    val description: String,
    val finalUrl: String,
)
