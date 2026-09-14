package com.starter.app.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * The auth token and login state, plus the two settings the user picks for themselves — language
 * and theme. Feature-specific preferences (a sync stamp, a last-opened id) belong in the feature's
 * own data module, not here.
 *
 * An interface over [AppDataStoreImpl] rather than the class itself: the implementation wraps a
 * `DataStore`, and anything holding one — a ViewModel, a repo — could not be unit tested without
 * dragging Robolectric in behind it.
 */
interface AppDataStore {

    suspend fun getAuthToken(): String

    suspend fun isLoggedIn(): Boolean

    suspend fun saveAuthToken(token: String)

    suspend fun clearAuthToken()

    suspend fun setLoggedIn(loggedIn: Boolean)

    /**
     * True while a non-blank token *and* the logged-in flag are both stored — the one definition of
     * "there is a session", so nothing has to re-derive it from a database row.
     *
     * **Deliberately un-caught:** a read failure reaches the collector as an `IOException` rather
     * than being smoothed into `false`. "Could not read" and "signed out" call for opposite
     * responses, and only `SessionGuard.observeSessionState` is in a position to tell them apart.
     */
    fun observeHasSession(): Flow<Boolean>

    /**
     * The stored language choice, or `null` while the user has never picked one. Kept as the raw
     * tag rather than a resolved locale, so the domain decides what an unset value means.
     */
    suspend fun getLanguage(): String?

    fun observeLanguage(): Flow<String?>

    suspend fun saveLanguage(language: String)

    /** The stored theme choice, or `null` while the user has never picked one. */
    fun observeThemeMode(): Flow<String?>

    suspend fun saveThemeMode(mode: String)

    suspend fun clearAll()
}
