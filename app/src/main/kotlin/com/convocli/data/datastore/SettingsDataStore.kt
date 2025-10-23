package com.convocli.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Settings DataStore for user preferences.
 *
 * Provides type-safe keys for all app settings.
 */
object SettingsKeys {
    val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    val DEFAULT_SHELL = stringPreferencesKey("default_shell")
    val FONT_SIZE = intPreferencesKey("font_size")
    val MAX_HISTORY_SIZE = intPreferencesKey("max_history_size")
}

/**
 * Extension property to access settings DataStore.
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
