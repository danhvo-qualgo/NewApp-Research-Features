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
            Task: Classify a message for cybersecurity risk.
            
            Input:
              - MESSAGE: {message}
              - CONTEXT: {context}
            
            Output: Valid JSON
              - category must be one of "Safe", "Scam" or "Unverified"
              {
                "category": "Safe|Scam|Unverified", 
                "reasons":[
                    {
                        "title":"brief about indicator",
                        "description":"short explanation why it's not safe"
                    }
                ]
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