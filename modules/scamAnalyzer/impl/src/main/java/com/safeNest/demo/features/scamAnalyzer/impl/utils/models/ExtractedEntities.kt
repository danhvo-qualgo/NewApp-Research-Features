package com.safeNest.demo.features.scamAnalyzer.impl.utils.models

data class ExtractedEntities(
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val urls: List<String> = emptyList(),
    val domains: List<String> = emptyList(),
) {
    val isEmpty: Boolean
        get() = phones.isEmpty() && emails.isEmpty() && urls.isEmpty() && domains.isEmpty()
}
