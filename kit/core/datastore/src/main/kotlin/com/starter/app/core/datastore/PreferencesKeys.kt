package com.starter.app.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_ID = stringPreferencesKey("user_id")
    val STATUS_LAST_SYNC_AT = longPreferencesKey("status_last_sync_at")

    /** The driver's language choice — "system", "en" or "ar". See `AppLanguage`. */
    val LANGUAGE = stringPreferencesKey("language")

    /** The driver's theme choice — "system", "light" or "dark". See `ThemeMode`. */
    val THEME_MODE = stringPreferencesKey("theme_mode")
}
