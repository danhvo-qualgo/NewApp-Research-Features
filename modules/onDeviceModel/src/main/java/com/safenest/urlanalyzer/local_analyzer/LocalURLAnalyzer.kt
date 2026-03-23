/*
 * LocalURLAnalyzer.kt — Runs all sub-analyses and produces analyzer data.
 *
 * Mirrors iOS LocalURLAnalyzer.swift and Python local_analyzer/__init__.py.
 */

package com.safenest.urlanalyzer.local_analyzer

import com.safenest.urlanalyzer.AnalyzerData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.URI

class LocalURLAnalyzer(
    private val brandNames: List<String>,
    private val brandDomains: Set<String>,
    private val pageTimeout: Long = 10_000L
) {

    /**
     * Run all sub-analyses in parallel and return combined result.
     */
    suspend fun analyze(url: String): AnalyzerData = coroutineScope {
        val domain = extractDomain(url)

        // CPU-only analyses (instant)
        val homograph = HomographAnalyzer.analyze(domain)
        val typosquat = TyposquatAnalyzer.analyze(domain, brandNames, brandDomains)

        // Network I/O analyses (parallel)
        val sslDeferred = async { SSLChecker.analyze(url) }
        val pageDeferred = async { PageFetcher.analyze(url, pageTimeout) }

        AnalyzerData(
            homograph = homograph,
            typosquat = typosquat,
            ssl = sslDeferred.await(),
            scamDB = mapOf("found" to false),
            pageInfo = pageDeferred.await()
        )
    }

    private fun extractDomain(url: String): String {
        return try {
            URI(url).host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
