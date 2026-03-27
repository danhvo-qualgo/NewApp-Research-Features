package com.safeNest.demo.features.urlGuard.impl.data.store

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlGuardStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore("url_guard_store")

    private val enableFormCheckKey = booleanPreferencesKey("enable_form_check")
    private val countOpenTelegram = intPreferencesKey("count_open_telegram")
    private val linkToTelegramBot = stringPreferencesKey("link_to_telegram_bot")



    suspend fun setEnableFormCheck(enabled: Boolean) {
        context.dataStore.edit { it[enableFormCheckKey] = enabled }
    }

    suspend fun isFormCheckEnabled(): Boolean {
        return context.dataStore
            .data
            .map { it[enableFormCheckKey] }
            .firstOrNull()
            ?: false
    }

    fun observeFormCheckEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[enableFormCheckKey] ?: true }
    }

    suspend fun increaseCountOpenTelegram() {
        context.dataStore.edit {
            val count = it[countOpenTelegram] ?: 0
            it[countOpenTelegram] = count + 1
        }
    }

    suspend fun getCountOpenTelegram(): Int {
        return context.dataStore
            .data
            .map { it[countOpenTelegram] }
            .firstOrNull() ?: 0
    }

    suspend fun resetCount() {
        context.dataStore.edit { it[countOpenTelegram] = 0 }
    }

    suspend fun setTelegramBotLink(link: String) {
        context.dataStore.edit { it[linkToTelegramBot] = link }
    }

    suspend fun getTelegramBotLink(): String {
        return context.dataStore.data
            .map { it[linkToTelegramBot] }
            .firstOrNull() ?: ""
    }
}
