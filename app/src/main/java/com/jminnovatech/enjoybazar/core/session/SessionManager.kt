package com.jminnovatech.joymart.core.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("joymart_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
        private const val KEY_ROLE = "role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LOGGED_IN = "logged_in"
    }

    // ✅ THIS WAS MISSING
    fun saveSession(
        userId: Int,
        token: String,
        role: String,
        userName: String? = null
    ) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role)
            .putString(KEY_USER_NAME, userName ?: "Customer")
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, "Customer")

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
