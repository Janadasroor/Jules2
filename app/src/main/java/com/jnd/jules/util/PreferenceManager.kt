package com.jnd.jules.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "jules_prefs"
    private const val KEY_API_KEY = "api_key"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private const val KEY_THEME_MODE = "theme_mode"

    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }
    
    // Theme Mode: 0 = System, 1 = Light, 2 = Dark
    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, 0)
    }
    
    fun saveThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }
}
