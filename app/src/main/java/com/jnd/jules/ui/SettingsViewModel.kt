package com.jnd.jules.ui

import androidx.lifecycle.ViewModel
import com.jnd.jules.util.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _apiKey = MutableStateFlow(PreferenceManager.getApiKey() ?: "")
    val apiKey = _apiKey.asStateFlow()

    private val _themeMode = MutableStateFlow(PreferenceManager.getThemeMode())
    val themeMode = _themeMode.asStateFlow()

    fun updateApiKey(newKey: String) {
        _apiKey.value = newKey
    }

    fun saveApiKey() {
        PreferenceManager.saveApiKey(_apiKey.value)
    }

    fun updateThemeMode(mode: Int) {
        _themeMode.value = mode
        PreferenceManager.saveThemeMode(mode)
    }
}
