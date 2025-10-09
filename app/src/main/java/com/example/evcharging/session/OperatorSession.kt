package com.example.evcharging.session


import android.content.Context
import android.content.SharedPreferences

object OperatorSession {
    private const val PREF = "ev_session"
    private lateinit var prefs: SharedPreferences

    fun initialize(ctx: Context) {
        if (!::prefs.isInitialized) {
            prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        }
    }

    fun setAuth(jwt: String, userId: String, fullName: String, email: String, role: String) {
        prefs.edit()
            .putString("jwt", jwt)
            .putString("userId", userId)
            .putString("fullName", fullName)
            .putString("email", email)
            .putString("role", role)
            .apply()
    }

    fun getJwt(): String? = prefs.getString("jwt", null)
    fun getRole(): String? = prefs.getString("role", null)
    // add other getters as needed
}
