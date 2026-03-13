package com.safeNest.demo.features.urlGuard1.impl.urlGuard1.networking

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
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
    val text: String,
    @SerialName("use_llm") val useLlm: Boolean = false
)

// ── Response ──────────────────────────────────────────────────────────────────

@Serializable
data class AnalyzeTextResponse(
    @SerialName("is_scam") val isScam: Boolean = false,
    @SerialName("scam_categories") val scamCategories: List<String> = emptyList(),
    val confidence: Double = 0.0,
    @SerialName("risk_level") val riskLevel: String = "",
    val evidence: List<String> = emptyList(),
    @SerialName("detected_entities") val detectedEntities: DetectedEntities? = null
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
        "https://safenest-public.danhtran94.dev:8443/llm-entry-service/v1/scam/analyze-text"

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

    /**
     * Analyze [text] (URL, message, etc.) for scam / phishing content.
     *
     * Usage:
     * ```
     * val result = ScamApiClient.checkUrl("https://evil.com")
     * if (result?.isScam == true) { ... }
     * ```
     *
     * @return [AnalyzeTextResponse] on success, or null if the request failed.
     */
    suspend fun checkUrl(text: String): AnalyzeTextResponse? {
        return try {
            httpClient
                .post(ENDPOINT) {
                    contentType(ContentType.Application.Json)
                    setBody(AnalyzeTextRequest(text = text))
                }
                .body<AnalyzeTextResponse>()
        } catch (e: Exception) {
            Log.e(TAG, "checkUrl failed: ${e.message}", e)
            null
        }
    }

    /**
     * Fetches the app category from the Google Play Store page.
     *
     * Strategy:
     *  1. Looks for `"applicationCategory"` inside the JSON-LD <script> tag (most reliable).
     *  2. Falls back to the `/store/apps/category/` link text in the page HTML.
     *
     * @return Human-readable category string (e.g. "Communication", "Social Networking")
     *         or null if unavailable / not found.
     */
    suspend fun fetchPlayStoreCategory(packageName: String): String? {
        return try {
            val pageUrl = "https://play.google.com/store/apps/details?id=$packageName&hl=en"
            val html = httpClient.get(pageUrl) {
                header("Accept-Language", "en-US,en;q=0.9")
                header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36"
                )
            }.body<String>()

            // ── Strategy 1: JSON-LD applicationCategory ──────────────────────
            val jsonLdMatch = Regex(""""applicationCategory"\s*:\s*"([^"]+)"""").find(html)
            if (jsonLdMatch != null) {
                return jsonLdMatch.groupValues[1]
                    .removeSuffix("Application")
                    .replace(Regex("([A-Z])"), " $1")
                    .trim()
            }

            // ── Strategy 2: category link href ───────────────────────────────
            Regex("""/store/apps/category/[^"]+">([^<]+)<""").find(html)
                ?.groupValues?.get(1)?.trim()
        } catch (e: Exception) {
            Log.e(TAG, "fetchPlayStoreCategory failed [$packageName]: ${e.message}")
            null
        }
    }

    /** Call this when the app is destroyed to free resources. */
    fun close() {
        httpClient.close()
    }
}
