package com.example.evcharging.session

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREFS_NAME = "user_session"
    private const val KEY_USER_NIC = "user_nic"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private lateinit var prefs: SharedPreferences
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun login(nic: String, name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_NIC, nic)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getUserNIC(): String? {
        return prefs.getString(KEY_USER_NIC, null)
    }
    
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    fun getUserInfo(): UserInfo? {
        return if (isLoggedIn()) {
            UserInfo(
                nic = getUserNIC() ?: "",
                name = getUserName() ?: "",
                email = getUserEmail() ?: ""
            )
        } else {
            null
        }
    }
}

data class UserInfo(
    val nic: String,
    val name: String,
    val email: String
)