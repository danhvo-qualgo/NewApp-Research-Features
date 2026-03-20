package com.safeNest.demo.features.scamAnalyzer.impl.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalyzeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyzeStore @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val Context.dataStore by preferencesDataStore("analyze_store")

    private val modeKey = intPreferencesKey("analyze_mode")
    private val customPromptKey = androidx.datastore.preferences.core.stringPreferencesKey("custom_prompt")

    companion object {
        const val DEFAULT_PROMPT = """
        Classify this message as a cybersecurity threat.

        Status codes:
        0=Safe, 1=Scam, 2=Unverified

        Safe: No phishing/scam indicators.
        Scam: Clear signals — impersonation, urgency, fake links, threats, sensitive info requests.
        Unverified: Suspicious but insufficient evidence.

        Rules:
        - Return ONLY valid JSON, no markdown.
        - If status = 0, reasons MUST be [].
        - If status != 0, reasons MUST contain at least one item.

        {"status":0|1|2,"reasons":[{"title":"string","description":"string"}]}

        MESSAGE: {message}
        CONTEXT: {context}
    """
    }

    suspend fun setMode(mode: AnalyzeMode) {
        context.dataStore.edit {
            it[modeKey] = mode.toInt()
        }
    }

    suspend fun getMode(): AnalyzeMode {
        return context.dataStore
            .data.map { it[modeKey] }
            .firstOrNull()
            ?.toMode()
            ?: AnalyzeMode.Local
    }
    
    suspend fun setCustomPrompt(prompt: String) {
        context.dataStore.edit {
            it[customPromptKey] = prompt
        }
    }
    
    suspend fun getCustomPrompt(): String {
        return context.dataStore
            .data.map { it[customPromptKey] }
            .firstOrNull()
            ?: DEFAULT_PROMPT.trimIndent()
    }

    private fun AnalyzeMode.toInt(): Int = when (this) {
        AnalyzeMode.Local -> 0
        AnalyzeMode.Remote -> 1
    }

    private fun Int.toMode(): AnalyzeMode {
        if (this == 0) return AnalyzeMode.Local

        return AnalyzeMode.Remote
    }
}