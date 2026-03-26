package com.safeNest.demo.features.urlGuard.impl.data.store

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    suspend fun setEnableFormCheck(enabled: Boolean) {
        context.dataStore.edit { it[enableFormCheckKey] = enabled }
    }

    suspend fun isFormCheckEnabled(): Boolean {
        return context.dataStore
            .data
            .map { it[enableFormCheckKey] }
            .firstOrNull()
            ?: true
    }

    fun observeFormCheckEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[enableFormCheckKey] ?: true }
    }
}
