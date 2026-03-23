/*
 * PageFetcher.kt — HTTP GET + HTML title/description extraction.
 *
 * Mirrors iOS PageFetcher.swift and Python local_analyzer/page_fetcher.py.
 */

package com.safenest.urlanalyzer.local_analyzer

import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object PageFetcher {

    suspend fun analyze(url: String, timeoutMs: Long = 10_000L): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "reachable" to false,
            "statusCode" to 0,
            "title" to "",
            "description" to "",
            "finalUrl" to ""
        )

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMs.toInt()
            connection.readTimeout = timeoutMs.toInt()
            connection.instanceFollowRedirects = true
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            )

            // Accept any cert (match Python verify=False)
            if (connection is HttpsURLConnection) {
                val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAll, SecureRandom())
                connection.sslSocketFactory = sslContext.socketFactory
                connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
            }

            try {
                connection.connect()
                val statusCode = connection.responseCode
                result["statusCode"] = statusCode
                result["finalUrl"] = connection.url.toString()
                result["reachable"] = statusCode in 200..399

                if (result["reachable"] == true) {
                    val html = connection.inputStream.bufferedReader()
                        .use { it.readText().take(100_000) } // Limit to 100KB
                    result["title"] = extractTitle(html)
                    result["description"] = extractDescription(html)
                }
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            // Request failed — reachable stays false
        }

        return result
    }

    // MARK: - HTML Parsing (simple regex, no WebView)

    private fun extractTitle(html: String): String {
        val pattern = Regex("<title[^>]*>(.*?)</title>", setOf( RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val match = pattern.find(html) ?: return ""
        return match.groupValues[1].trim().take(200)
    }

    private fun extractDescription(html: String): String {
        val patterns = listOf(
            Regex("""<meta[^>]+name=["']description["'][^>]+content=["']([^"']*)["']""", RegexOption.IGNORE_CASE),
            Regex("""<meta[^>]+content=["']([^"']*)["'][^>]+name=["']description["']""", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val match = pattern.find(html) ?: continue
            return match.groupValues[1].trim().take(500)
        }
        return ""
    }
}
