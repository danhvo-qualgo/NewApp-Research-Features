/*
 * SMSAnalyzerOrchestrator.kt — Full SMS analysis pipeline.
 *
 * 1. Entity extraction (phone, URL, email)
 * 2. PII redaction
 * 3. Gate 1 URL analysis for each extracted URL
 * 4. Signals summary
 * 5. TextAnalyzerClassifier (LLM)
 */
package com.safenest.urlanalyzer.text

import com.safenest.urlanalyzer.url.gate1.Gate1Classifier

data class SMSAnalysisResult(
    val verdict: String,
    val riskScore: Double,
    val keyFindings: List<KeyFinding>,
    val rawResponse: String,
    val redactedMessage: String,
    val entities: ExtractedEntities,
    val signalsSummary: String,
    val elapsed: Double
)

class SMSAnalyzerOrchestrator(
    private val textClassifier: TextAnalyzerClassifier,
    private val gate1: Gate1Classifier?
) {
    suspend fun analyze(message: String): SMSAnalysisResult {
        val start = System.nanoTime()

        // 1. Entity extraction
        val entities = EntityExtractor.extract(message)

        // 2. PII redaction
        val redacted = PIIRedactor.redact(message, entities)

        // 3. URL analysis via Gate 1
        val urlSignals = mutableListOf<String>()
        if (gate1 != null) {
            for (url in entities.urls) {
                try {
                    val result = gate1.classify(url)
                    if (result.verdict != "safe") {
                        val findings = result.keyFindings.joinToString(", ") { it.description }
                        urlSignals.add("[SCAM SIGNAL] URL '$url' analyzed as ${result.verdict} (risk: ${String.format("%.2f", result.riskScore)}). $findings")
                    }
                } catch (e: Exception) {
                    // Skip failed URL analysis
                }
            }
        }

        // 4. Build signals summary
        val signals = if (urlSignals.isNotEmpty()) {
            urlSignals.joinToString("\n")
        } else {
            ""
        }

        // 5. LLM analysis
        textClassifier.activate()
        val result = textClassifier.analyze(redacted, if (signals.isBlank()) null else signals)

        val elapsed = (System.nanoTime() - start) / 1_000_000_000.0

        return SMSAnalysisResult(
            verdict = result.verdict,
            riskScore = result.riskScore,
            keyFindings = result.keyFindings,
            rawResponse = result.rawResponse,
            redactedMessage = redacted,
            entities = entities,
            signalsSummary = signals.ifBlank { "No scam signals detected from entity analysis." },
            elapsed = elapsed
        )
    }
}
