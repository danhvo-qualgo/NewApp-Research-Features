package com.safeNest.demo.features.home.impl.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore("home_store")

    private val dnsPermissionKey = booleanPreferencesKey("granted_private_dns_permission")

    suspend fun setDnsPermission(isGranted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[dnsPermissionKey] = isGranted
        }
    }

    suspend fun isDnsPermissionGranted(): Boolean {
        context.dataStore.data.first()
        return context.dataStore.data.map { it[dnsPermissionKey] }.firstOrNull() ?: false
    }

    fun getDnsPermissionFlow(): Flow<Boolean> {
        return context.dataStore.data.map { it[dnsPermissionKey] ?: false }
    }
}