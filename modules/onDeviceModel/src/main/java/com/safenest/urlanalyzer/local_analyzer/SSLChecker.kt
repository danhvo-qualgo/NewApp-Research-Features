/*
 * SSLChecker.kt — TLS certificate extraction.
 *
 * Mirrors iOS SSLChecker.swift and Python local_analyzer/ssl_checker.py.
 *
 * Unlike iOS which needs a custom DER parser, Android's X509Certificate
 * natively provides issuer, expiry, and subject alt names.
 */

package com.safenest.urlanalyzer.local_analyzer

import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object SSLChecker {

    suspend fun analyze(url: String): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>(
            "valid" to false,
            "issuer" to "",
            "expiresAt" to "",
            "daysUntilExpiry" to 0,
            "protocol" to "",
            "subjectAltNames" to emptyList<String>()
        )

        if (!url.startsWith("https://")) return result

        try {
            val connection = URL(url).openConnection() as? HttpsURLConnection ?: return result
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            // Trust all certs for inspection (we evaluate validity separately)
            val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAll, SecureRandom())
            connection.sslSocketFactory = sslContext.socketFactory
            connection.hostnameVerifier = HostnameVerifier { _, _ -> true }

            try {
                connection.connect()

                val certs = connection.serverCertificates
                if (certs.isNotEmpty() && certs[0] is X509Certificate) {
                    val leaf = certs[0] as X509Certificate

                    // Check validity
                    try {
                        leaf.checkValidity()
                        result["valid"] = true
                    } catch (_: Exception) {
                        result["valid"] = false
                    }

                    // Issuer — extract CN or O from issuer DN
                    val issuerDN = leaf.issuerX500Principal.name
                    result["issuer"] = extractCNFromDN(issuerDN)
                        ?: extractFieldFromDN(issuerDN, "O")
                        ?: issuerDN

                    // Expiry
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    result["expiresAt"] = formatter.format(leaf.notAfter)

                    val daysUntil = ((leaf.notAfter.time - System.currentTimeMillis()) / 86_400_000.0)
                        .toLong().toInt()
                    result["daysUntilExpiry"] = daysUntil

                    // Subject Alt Names
                    try {
                        val sans = leaf.subjectAlternativeNames?.mapNotNull { entry ->
                            if (entry.size >= 2 && entry[0] == 2) entry[1] as? String else null
                        } ?: emptyList()
                        result["subjectAltNames"] = sans
                    } catch (_: Exception) {}

                    // TLS protocol
                    result["protocol"] = "TLS"
                }
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            // Connection failed — valid stays false
        }

        return result
    }

    private fun extractCNFromDN(dn: String): String? {
        val regex = Regex("""CN=([^,]+)""")
        return regex.find(dn)?.groupValues?.get(1)?.trim()
    }

    private fun extractFieldFromDN(dn: String, field: String): String? {
        val regex = Regex("""$field=([^,]+)""")
        return regex.find(dn)?.groupValues?.get(1)?.trim()
    }
}
