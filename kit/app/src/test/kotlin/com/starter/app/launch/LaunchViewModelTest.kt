package com.starter.app.launch

import com.starter.app.core.datastore.AppDataStore
import com.starter.app.core.datastore.session.SessionGuard
import com.starter.app.core.testing.MainDispatcherRule
import com.starter.app.navigation.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/** What the app opens on, decided while the system splash is held. */
@OptIn(ExperimentalCoroutinesApi::class)
class LaunchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val store = FakeStore()

    private fun viewModel() = LaunchViewModel(SessionGuard(store))

    @Test
    fun `a stored session opens on Home`() = runTest(mainDispatcherRule.dispatcher) {
        store.hasSession.value = true
        val vm = viewModel()
        runCurrent()

        assertEquals(Screen.Home, vm.startDestination.value)
    }

    @Test
    fun `no session opens on Home until the app has a sign-in screen`() =
        runTest(mainDispatcherRule.dispatcher) {
            store.hasSession.value = false
            val vm = viewModel()
            runCurrent()

            assertEquals(Screen.Home, vm.startDestination.value)
        }

    @Test
    fun `an unreadable store keeps the splash up rather than deciding`() =
        runTest(mainDispatcherRule.dispatcher) {
            store.failNextRead = true
            store.hasSession.value = true
            val vm = viewModel()
            runCurrent()
            assertNull("decided on an unreadable store", vm.startDestination.value)

            // SessionGuard retries after its backoff; the second read succeeds.
            advanceUntilIdle()
            assertEquals(Screen.Home, vm.startDestination.value)
        }

    /** Only the session stream matters here; the rest of the store is never reached. */
    private class FakeStore : AppDataStore {
        val hasSession = MutableStateFlow(false)
        var failNextRead = false

        override fun observeHasSession(): Flow<Boolean> = flow {
            if (failNextRead) {
                failNextRead = false
                throw IOException("store unreadable")
            }
            emitAll(hasSession)
        }

        override suspend fun getAuthToken(): String = ""
        override suspend fun isLoggedIn(): Boolean = hasSession.value
        override suspend fun saveAuthToken(token: String) = Unit
        override suspend fun clearAuthToken() = Unit
        override suspend fun setLoggedIn(loggedIn: Boolean) = Unit
        override suspend fun getLanguage(): String? = null
        override fun observeLanguage(): Flow<String?> = emptyFlow()
        override suspend fun saveLanguage(language: String) = Unit
        override fun observeThemeMode(): Flow<String?> = emptyFlow()
        override suspend fun saveThemeMode(mode: String) = Unit
        override suspend fun clearAll() = Unit
    }
}
