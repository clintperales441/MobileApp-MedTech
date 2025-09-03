package com.example.medtech.helper

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate


object ThemeManager {
    private const val PREFERENCES_NAME = "settings"
    private const val KEY_DARK_MODE = "dark_mode_enabled"

    private val themeListeners = mutableListOf<() -> Unit>()

    fun initialize(context: Context) {
        val isDarkMode = getPreferences(context).getBoolean(KEY_DARK_MODE, false)
        setDarkMode(isDarkMode)
    }

    fun setDarkMode(enabled: Boolean, context: Context? = null) {
        val mode = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        context?.let {
            getPreferences(it).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        }

        themeListeners.forEach { it.invoke() }
    }

    fun isDarkModeEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_DARK_MODE, false)
    }

    fun isNightModeActive(context: Context): Boolean {
        return (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }

    fun toggleDarkMode(context: Context): Boolean {
        val newState = !isDarkModeEnabled(context)
        setDarkMode(newState, context)
        return newState
    }

    fun addThemeChangeListener(listener: () -> Unit) {
        themeListeners.add(listener)
    }

    fun removeThemeChangeListener(listener: () -> Unit) {
        themeListeners.remove(listener)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
}