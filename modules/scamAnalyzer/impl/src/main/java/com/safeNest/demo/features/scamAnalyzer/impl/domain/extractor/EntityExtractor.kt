package com.safeNest.demo.features.scamAnalyzer.impl.domain.extractor

data class ExtractedEntities(
    val phoneNumbers: List<String>,
    val emails: List<String>,
    val urls: List<String>,
    val domains: List<String>
)

interface EntityExtractor {
    fun extract(text: String): ExtractedEntities
}