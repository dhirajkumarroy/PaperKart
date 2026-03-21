package com.example.paperkart.core.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("paperkart_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    // ✅ Use 'edit { ... }' for cleaner, safer SharedPreferences updates
    fun saveToken(token: String?) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun saveRefreshToken(token: String?) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun saveUserData(name: String?, email: String?) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply() // ✅ Only one apply() at the end
    }

    fun getToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, "User") // Default to "User"
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    // ✅ Added helper to check if user is logged in
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}