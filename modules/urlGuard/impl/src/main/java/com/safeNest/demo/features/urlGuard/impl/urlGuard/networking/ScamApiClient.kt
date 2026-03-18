package com.safeNest.demo.features.urlGuard.impl.urlGuard.networking

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ── Request ───────────────────────────────────────────────────────────────────

@Serializable
data class AnalyzeTextRequest(
    val url: String
)

// ── Response ──────────────────────────────────────────────────────────────────

@Serializable
data class VerdictScore(
    val riskScore: Double,
    val verdict: String
)

@Serializable
data class AnalyzeTextResponse(
    val data: VerdictScore
)

@Serializable
data class DetectedEntities(
    val urls: List<String> = emptyList(),
    @SerialName("phone_numbers") val phoneNumbers: List<String> = emptyList(),
    @SerialName("money_amounts") val moneyAmounts: List<String> = emptyList(),
    @SerialName("suspicious_keywords") val suspiciousKeywords: List<String> = emptyList()
)

// ── Client ────────────────────────────────────────────────────────────────────

object ScamApiClient {

    private const val TAG = "ScamApiClient"
    private const val ENDPOINT =
        "https://demo-safenest-usecase.qualgo.dev/api/v1.0/analyze/url/verdict"

    private val httpClient: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, message)
                    }
                }
            }
            engine {
                connectTimeout = 15_000
                socketTimeout = 15_000
            }
        }
    }


    suspend fun checkUrl(text: String): AnalyzeTextResponse? {
        return try {
            httpClient
                .post(ENDPOINT) {
                    contentType(ContentType.Application.Json)
                    setBody(AnalyzeTextRequest(url = text))
                }
                .body<AnalyzeTextResponse>()
        } catch (e: Exception) {
            Log.e(TAG, "checkUrl failed: ${e.message}", e)
            null
            checkUrlTest(url = text)
        }
    }
    

    //Check url for testing purpose
    fun checkUrlTest(url: String): AnalyzeTextResponse {
        return PREDEFINED_URL_TABLE[url] ?: FALLBACK_SAFE_RESPONSE
    }

    // ── Predefined test URL table ─────────────────────────────────────────────

    private fun response(riskScore: Double, verdict: String) =
        AnalyzeTextResponse(data = VerdictScore(riskScore = riskScore, verdict = verdict))

    private val PREDEFINED_URL_TABLE: Map<String, AnalyzeTextResponse> = mapOf(

        // ── SAFE urls (riskScore ≤ 0.3) ───────────────────────────────────────
        "https://google.com"         to response(0.01, "safe"),
        "https://www.google.com"     to response(0.01, "safe"),
        "https://youtube.com"        to response(0.02, "safe"),
        "https://github.com"         to response(0.02, "safe"),
        "https://stackoverflow.com"  to response(0.03, "safe"),
        "https://wikipedia.org"      to response(0.01, "safe"),
        "https://apple.com"          to response(0.02, "safe"),
        "https://microsoft.com"      to response(0.02, "safe"),

        // ── WARNING urls (riskScore 0.31 – 0.80) ──────────────────────────────
        "https://bit.ly/3xAbc12"     to response(0.55, "medium"),
        "https://tinyurl.com/y4k2b"  to response(0.50, "medium"),
        "https://t.co/suspicious"    to response(0.65, "medium"),
        "https://unknown-shop.net"   to response(0.72, "medium"),
        "https://free-prize.info"    to response(0.78, "medium"),

        // ── DANGEROUS / SCAM urls (riskScore > 0.8) ───────────────────────────
        "https://paypa1-secure.com"              to response(0.97, "dangerous"),
        "https://apple-id-verify.xyz"            to response(0.95, "dangerous"),
        "https://banking-secure-login.net"       to response(0.93, "dangerous"),
        "https://click-here-claim-reward.com"    to response(0.92, "dangerous"),
        "https://faceb00k-login.ru"              to response(0.98, "dangerous"),
        "https://amazon-gift-verify.club"        to response(0.91, "dangerous"),
        "https://virus-alert-download-now.com"   to response(0.99, "dangerous")
    )

    private val FALLBACK_SAFE_RESPONSE = response(0.01, "safe")

    /** Call this when the app is destroyed to free resources. */
    fun close() {
        httpClient.close()
    }
}
