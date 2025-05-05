
package com.example.myapplication.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getLoginState(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun saveLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun getRememberMeState(): Boolean {
        return sharedPreferences.getBoolean("rememberMe", false)
    }

    fun saveRememberMeState(rememberMe: Boolean) {
        sharedPreferences.edit().putBoolean("rememberMe", rememberMe).apply()
    }
}