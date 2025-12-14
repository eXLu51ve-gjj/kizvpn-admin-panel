package com.kizvpn.admin.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthRepository(private val context: Context) {
    
    companion object {
        private val API_URL_KEY = stringPreferencesKey("api_url")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    }
    
    val apiUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[API_URL_KEY]
    }
    
    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }
    
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BIOMETRIC_ENABLED_KEY] ?: false
    }
    
    suspend fun saveApiUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[API_URL_KEY] = url
        }
    }
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        val apiUrl = context.dataStore.data.map { it[API_URL_KEY] }
        val token = context.dataStore.data.map { it[TOKEN_KEY] }
        // Простая проверка - в реальности нужно проверять токен через API
        return apiUrl.toString().isNotEmpty() && token.toString().isNotEmpty()
    }
}


