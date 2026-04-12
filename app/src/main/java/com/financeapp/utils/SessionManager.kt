package com.financeapp.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager handles persistent user sessions.
 * It stores login state and user-specific details.
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val PREF_NAME = "FinanceProSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_AUTH_TOKEN = "authToken"
        private const val KEY_PIN = "userPin"
        private const val KEY_BIOMETRIC_ENABLED = "biometricEnabled"
    }

    /**
     * Creates a login session.
     */
    fun createLoginSession(email: String, token: String?) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    /**
     * Checks if user is logged in.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Clears session (Logout).
     */
    fun logout() {
        editor.clear()
        editor.apply()
    }

    /**
     * Retrieves logged-in user email.
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Sets or updates the security PIN.
     */
    fun setPin(pin: String) {
        editor.putString(KEY_PIN, pin)
        editor.apply()
    }

    /**
     * Retrieves the security PIN.
     */
    fun getPin(): String? {
        return prefs.getString(KEY_PIN, null)
    }

    /**
     * Checks if PIN is set.
     */
    fun isPinSet(): Boolean {
        return !getPin().isNullOrEmpty()
    }

    /**
     * Toggle biometric authentication preference.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        editor.putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
        editor.apply()
    }

    /**
     * Checks if biometric authentication is enabled.
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
}
