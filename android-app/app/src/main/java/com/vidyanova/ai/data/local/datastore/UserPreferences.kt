package com.vidyanova.ai.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
        private val STUDENT_NAME_KEY = stringPreferencesKey("student_name")
        private val CLASS_LEVEL_KEY = intPreferencesKey("class_level")
        private val PREFERRED_LANGUAGE_KEY = stringPreferencesKey("preferred_language")
        private val PREFERRED_STYLE_KEY = stringPreferencesKey("preferred_style")
        private val SYNC_CONSENT_KEY = booleanPreferencesKey("sync_consent")
    }

    suspend fun getOrCreateDeviceId(): String {
        var id: String? = null
        context.dataStore.edit { prefs ->
            val existing = prefs[DEVICE_ID_KEY]
            if (existing.isNullOrBlank()) {
                val newId = UUID.randomUUID().toString()
                prefs[DEVICE_ID_KEY] = newId
                id = newId
            } else {
                id = existing
            }
        }
        return id ?: UUID.randomUUID().toString()
    }

    val authTokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[AUTH_TOKEN_KEY]
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_TOKEN_KEY] = token
        }
    }

    val lastSyncTimeFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_SYNC_TIME_KEY] ?: 0L
    }

    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME_KEY] = timestamp
        }
    }

    val syncConsentFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        if (prefs.contains(SYNC_CONSENT_KEY)) prefs[SYNC_CONSENT_KEY] else null
    }

    suspend fun saveSyncConsent(consent: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SYNC_CONSENT_KEY] = consent
        }
    }

    val studentNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val savedName = prefs[STUDENT_NAME_KEY]
        if (!savedName.isNullOrBlank() && savedName != "Student") savedName else "Guest Student"
    }

    suspend fun saveStudentName(name: String) {
        if (name.isNotBlank()) {
            context.dataStore.edit { prefs ->
                prefs[STUDENT_NAME_KEY] = name.trim()
            }
        }
    }

    val classLevelFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[CLASS_LEVEL_KEY] ?: 10
    }

    suspend fun saveClassLevel(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[CLASS_LEVEL_KEY] = level
        }
    }

    val preferredLanguageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PREFERRED_LANGUAGE_KEY] ?: "Hinglish"
    }

    suspend fun savePreferredLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[PREFERRED_LANGUAGE_KEY] = language
        }
    }

    val preferredStyleFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PREFERRED_STYLE_KEY] ?: "Step-by-Step"
    }

    suspend fun savePreferredStyle(style: String) {
        context.dataStore.edit { prefs ->
            prefs[PREFERRED_STYLE_KEY] = style
        }
    }

    suspend fun saveStudentProfile(name: String, email: String, classLevel: Int) {
        context.dataStore.edit { prefs ->
            if (name.isNotBlank() && name != "Student") {
                prefs[STUDENT_NAME_KEY] = name.trim()
            }
            prefs[CLASS_LEVEL_KEY] = classLevel
            if (prefs[AUTH_TOKEN_KEY].isNullOrBlank()) {
                prefs[AUTH_TOKEN_KEY] = "offline_local_token_${UUID.randomUUID()}"
            }
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            val deviceId = prefs[DEVICE_ID_KEY]
            prefs.clear()
            if (!deviceId.isNullOrBlank()) {
                prefs[DEVICE_ID_KEY] = deviceId
            }
        }
    }
}
