package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.PhishingLlmAnalyzer
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities

object LlmEntityExtractor {

    suspend fun extract(
        text: String,
        analyzer: PhishingLlmAnalyzer,
        onProgress: (partialOutput: String) -> Unit,
    ): ExtractedEntities = withContext(Dispatchers.IO) {
        val tokens = StringBuilder()
        val prompt = buildEntityExtractionPrompt(text)

        var done = false
        analyzer.llmProcessing(
            prompt = prompt,
            onToken = { token ->
                tokens.append(token)
                onProgress(tokens.toString())
            },
            onDone = { done = true },
        )

        parseEntitiesFromLlmResponse(tokens.toString())
    }

    private fun parseEntitiesFromLlmResponse(response: String): ExtractedEntities {
        val phones = mutableListOf<String>()
        val emails = mutableListOf<String>()
        val urls = mutableListOf<String>()
        val domains = mutableListOf<String>()

        // The LLM returns JSON structured like:
        // {"phones": [...], "emails": [...], "urls": [...], "domains": [...]}
        // We parse each list with a tolerant regex approach to avoid a JSON library dependency.
        fun extractList(key: String): List<String> {
            val pattern = Regex(""""$key"\s*:\s*\[([^\]]*)\]""", RegexOption.DOT_MATCHES_ALL)
            val match = pattern.find(response) ?: return emptyList()
            val arrayContent = match.groupValues[1]
            return Regex(""""([^"]+)"""").findAll(arrayContent)
                .map { it.groupValues[1].trim() }
                .filter { it.isNotBlank() }
                .toList()
        }

        phones.addAll(extractList("phones"))
        emails.addAll(extractList("emails"))
        urls.addAll(extractList("urls"))
        domains.addAll(extractList("domains"))

        return ExtractedEntities(
            phones = phones,
            emails = emails,
            urls = urls,
            domains = domains,
        )
    }

    private fun buildEntityExtractionPrompt(text: String): String {
        return buildString {
            append("<|im_start|>system\n")
            append("You are an entity extraction assistant. ")
            append("Extract all contact and web entities from the given text. ")
            append("Reply ONLY with a valid JSON object and nothing else.")
            append("<|im_end|>\n")
            append("<|im_start|>user\n")
            append("Extract all entities from the following text.\n\n")
            append("Text:\n$text\n\n")
            append("Return a JSON object with exactly these keys: \"phones\", \"emails\", \"urls\", \"domains\".\n")
            append("Each key maps to an array of strings. Use an empty array if none are found.\n")
            append("<|im_end|>\n")
            append("<|im_start|>assistant\n")
        }
    }
}
