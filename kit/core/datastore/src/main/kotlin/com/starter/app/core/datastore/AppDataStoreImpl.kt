package com.starter.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * [ReplaceFileCorruptionHandler] rebuilds the file rather than leaving it unreadable for good.
 *
 * Without one, a `CorruptionException` is thrown on **every** read from then on, and no retry can
 * help — the app would be permanently unable to answer whether anyone is signed in. Rebuilding
 * costs the user a sign-in, which is recoverable; the alternative is not.
 *
 * Note what this does **not** do: it is invoked only for a corrupt file, never for an ordinary
 * `IOException`. A momentary read failure therefore never wipes anything — which is the separation
 * the rest of this file depends on, since a transient failure and a real sign-out call for opposite
 * responses.
 */
private val Context.preferencesStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

/** The app's one preferences file, as the Koin module hands it to [AppDataStoreImpl]. */
val Context.appPreferences: DataStore<Preferences> get() = preferencesStore

/**
 * Takes the [DataStore] rather than a `Context`, so the corruption and read-failure behaviour above
 * can be driven against a real store over a temporary file — on the JVM, with no device and no
 * Robolectric. Wiring it from a `Context` is `dataStoreModule`'s job.
 */
class AppDataStoreImpl(private val dataStore: DataStore<Preferences>) : AppDataStore {

    /**
     * **No fallback on this one, or on [isLoggedIn].** Both feed `SessionGuard`, and an empty token
     * read as "" would be indistinguishable from a signed-out user — the exact conflation that
     * turns a disk hiccup into a user thrown back to the login screen. The guard wants the
     * `IOException` so it can skip the write and change nothing else.
     */
    override suspend fun getAuthToken(): String = dataStore.data
        .map { it[PreferencesKeys.AUTH_TOKEN] }.first().orEmpty()

    override suspend fun isLoggedIn(): Boolean = dataStore.data
        .map { it[PreferencesKeys.IS_LOGGED_IN] }.first() ?: false

    override suspend fun saveAuthToken(token: String) {
        dataStore.edit { it[PreferencesKeys.AUTH_TOKEN] = token }
    }

    override suspend fun clearAuthToken() {
        dataStore.edit { it.remove(PreferencesKeys.AUTH_TOKEN) }
    }

    override suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_LOGGED_IN] = loggedIn }
    }

    /**
     * No retry and no fallback here either — `SessionGuard.observeSessionState` wraps this, and it
     * is the only thing that can report an unreadable store as `Unavailable` rather than letting it
     * pass for a sign-out.
     */
    override fun observeHasSession(): Flow<Boolean> = dataStore.data.map {
        !it[PreferencesKeys.AUTH_TOKEN].isNullOrBlank() && it[PreferencesKeys.IS_LOGGED_IN] == true
    }

    /**
     * A fallback *is* right here, unlike on the session reads above: an unreadable language means
     * "we do not know what they chose", which resolves to the same default as never having chosen.
     * Without it an `IOException` would propagate out of any data-layer call that reads the
     * language to localise a request, and fail that request over a preference.
     */
    override suspend fun getLanguage(): String? = dataStore.data
        .map { it[PreferencesKeys.LANGUAGE] }
        .catch { if (it is IOException) emit(null) else throw it }
        .first()

    override fun observeLanguage(): Flow<String?> =
        dataStore.data.map { it[PreferencesKeys.LANGUAGE] }.retryOnReadFailure()

    override suspend fun saveLanguage(language: String) {
        dataStore.edit { it[PreferencesKeys.LANGUAGE] = language }
    }

    override fun observeThemeMode(): Flow<String?> =
        dataStore.data.map { it[PreferencesKeys.THEME_MODE] }.retryOnReadFailure()

    override suspend fun saveThemeMode(mode: String) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
