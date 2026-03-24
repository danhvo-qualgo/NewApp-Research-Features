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
{
  "system_prompt": "You are a cybersecurity specialist explaining message scams to everyday users.\n\nGiven a MESSAGE and optional CONTEXT, classify the message as Safe, Scam, or Unverified and explain briefly.\n\nRespond ONLY with valid JSON. No markdown or extra text.\n\nOutput format (exact field order):\n{\"category\": string, \"reasons\": [{\"title\": string, \"description\": string}]}\n\nRules:\n- category must be exactly one of: Safe, Scam, Unverified\n- Do not output combined values like \"Safe|Scam\"\n- Max 3 reasons\n- Each description < 20 words\n- Use simple language for non-technical users\n\nPossible titles: Urgent Request, Fake Authority, Suspicious Link, Request for Personal Information, Financial Request, Unknown Sender, Too Good To Be True\n\nExample:\n{\"category\":\"Safe\",\"reasons\":[]}",
  "user_prompt_template": "MESSAGE:\n{message}\n\nCONTEXT:\n{context}"
}
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