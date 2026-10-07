package com.vidyanova.ai.data.network

import android.content.Context
import android.content.SharedPreferences

object AuthTokenStore {
    private const val PREFERENCES_NAME = "vidyanova_auth"
    private const val TOKEN_KEY = "jwt"

    @Volatile
    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun getToken(): String? =
        checkNotNull(preferences) { "AuthTokenStore must be initialized before use" }
            .getString(TOKEN_KEY, null)

    fun saveToken(token: String) {
        check(token.isNotBlank()) { "Authentication token must not be blank" }
        checkNotNull(preferences) { "AuthTokenStore must be initialized before use" }
            .edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun clearToken() {
        checkNotNull(preferences) { "AuthTokenStore must be initialized before use" }
            .edit()
            .remove(TOKEN_KEY)
            .apply()
    }
}
