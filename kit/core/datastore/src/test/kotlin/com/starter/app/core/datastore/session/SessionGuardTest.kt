package com.starter.app.core.datastore.session

import com.starter.app.core.datastore.AppDataStore
import com.starter.app.core.model.session.SessionState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * The guard that keeps a reply from outliving the session that asked for it, and the flow the whole
 * app reads "is anyone signed in" from.
 *
 * Everything here is about a window rather than a value: between a request going out and its answer
 * being written, the driver can sign out or somebody else can sign in. The failures this prevents
 * are all quiet ones — a driver's row restored after a logout, one account's data written over
 * another's, one account's `401` shown to another — so none of them would surface without tests.
 */
class SessionGuardTest {

    private val store = FakeStore()
    private val guard = SessionGuard(store)

    // ---- snapshots ----------------------------------------------------------------------------

    @Test
    fun `there is no snapshot without a token`() = runTest {
        store.token = ""

        assertNull(guard.snapshot())
    }

    @Test
    fun `there is no snapshot while the logged-in flag is down`() = runTest {
        store.loggedIn = false

        assertNull(guard.snapshot())
    }

    /**
     * The id and the token are one indivisible read, so a request can never carry one session's
     * token tagged with another's id.
     *
     * Driven by parking the token read and starting a sign-out from another coroutine: the sign-out
     * has to wait for the lock the snapshot holds, which is what makes the pair atomic rather than
     * merely adjacent.
     */
    @Test
    fun `a session change cannot interleave with taking a snapshot`() = runTest {
        store.blockTokenRead()

        val snapshot = async { guard.snapshot() }
        store.awaitTokenRead()
        val switching = launch { guard.renew { store.token = "second" } }
        runCurrent()

        assertFalse("renew must queue behind the snapshot", switching.isCompleted)

        store.releaseTokenRead()
        assertEquals("first", snapshot.await()?.token)
        switching.join()
    }

    // ---- guarding a write ----------------------------------------------------------------------

    @Test
    fun `a write for an older session is skipped`() = runTest {
        val stale = guard.snapshot()!!
        guard.renew { store.token = "second" }

        assertNull(guard.ifStillCurrent(stale) { "written" })
    }

    @Test
    fun `a write for the current session goes through`() = runTest {
        val session = guard.snapshot()!!

        assertEquals("written", guard.ifStillCurrent(session) { "written" })
    }

    /**
     * **The corruption case, and why the guard re-reads rather than comparing ids.**
     *
     * `ReplaceFileCorruptionHandler` can replace the whole preferences file without going through
     * a login or a logout, so the id never moves — an id comparison alone would still call this the
     * same session and write over a token that no longer exists.
     */
    @Test
    fun `a write is skipped when the token vanished without the id moving`() = runTest {
        val session = guard.snapshot()!!
        store.token = ""

        assertNull(guard.ifStillCurrent(session) { "written" })
    }

    /**
     * An unreadable store is not a confirmed sign-out. The write is skipped — failing closed — but
     * the exception is allowed out so nothing here can mistake it for the session having ended and
     * start clearing things.
     */
    @Test(expected = IOException::class)
    fun `a read failure during the check surfaces rather than passing for a lost session`() =
        runTest {
            val session = guard.snapshot()!!
            store.readFailure = IOException("disk")

            guard.ifStillCurrent(session) { "written" }
            Unit
        }

    // ---- clearing what a signed-out app must not keep -------------------------------------------

    @Test
    fun `clearing is declined while somebody is signed in`() = runTest {
        var cleared = false

        assertFalse(guard.ifStillSignedOut { cleared = true })
        assertFalse(cleared)
    }

    @Test
    fun `clearing runs once the session is gone`() = runTest {
        guard.renew { store.token = "" }
        var cleared = false

        assertTrue(guard.ifStillSignedOut { cleared = true })
        assertTrue(cleared)
    }

    // ---- the session flow ------------------------------------------------------------------------

    @Test
    fun `the flow reports a stored session as active`() = runTest {
        val states = mutableListOf<SessionState>()
        val job = launch { guard.observeSessionState().toList(states) }
        advanceUntilIdle()
        job.cancel()

        assertEquals(listOf(SessionState.Active(sessionId = 0)), states)
    }

    /**
     * **A fast account switch has to reach the collector.**
     *
     * The store can coalesce a logout and the login after it into a single emission, so what
     * arrives is `Active` followed by `Active`. With a bare `data object` those two are equal and
     * `distinctUntilChanged` drops the second — the app would never learn the driver had changed.
     * The id is what makes them different values.
     */
    @Test
    fun `a switch between two sessions is not swallowed as a duplicate`() = runTest {
        val states = mutableListOf<SessionState>()
        val job = launch { guard.observeSessionState().toList(states) }
        advanceUntilIdle()

        guard.renew { store.token = "second" }
        advanceUntilIdle()
        job.cancel()

        assertEquals(
            listOf(SessionState.Active(sessionId = 0), SessionState.Active(sessionId = 1)),
            states,
        )
    }

    /**
     * A read failure is reported as [SessionState.Unavailable] and then **retried** — the flow does
     * not end.
     *
     * Both of the obvious alternatives are wrong. A terminal `catch` would leave the collector on a
     * stale answer for the life of the subscription, with no way back once the store recovered; and
     * emitting `SignedOut` would make a momentary disk error indistinguishable from a logout, which
     * is a driver thrown off a run for nothing.
     */
    @Test
    fun `an unreadable store reports unavailable and then recovers on its own`() = runTest {
        store.sessionFailures = 1
        val states = mutableListOf<SessionState>()
        val job = launch { guard.observeSessionState().toList(states) }
        advanceUntilIdle()
        job.cancel()

        assertEquals(
            listOf(SessionState.Unavailable, SessionState.Active(sessionId = 0)),
            states,
        )
    }

    /** After recovering, later sign-outs and sign-ins still reach the collector. */
    @Test
    fun `changes after a recovery still arrive`() = runTest {
        store.sessionFailures = 1
        val states = mutableListOf<SessionState>()
        val job = launch { guard.observeSessionState().toList(states) }
        advanceUntilIdle()

        guard.renew { store.token = "" }
        advanceUntilIdle()
        job.cancel()

        assertEquals(
            listOf(
                SessionState.Unavailable,
                SessionState.Active(sessionId = 0),
                SessionState.SignedOut,
            ),
            states,
        )
    }

    /** Anything that is not a storage failure is a programming error and has to surface. */
    @Test(expected = IllegalStateException::class)
    fun `a programming error is not retried away`() = runTest {
        store.sessionError = IllegalStateException("boom")

        guard.observeSessionState().toList(mutableListOf())
    }
}

private class FakeStore : AppDataStore {

    /**
     * Writing either of these bumps [revisions], the way a real `renew` always writes
     * `IS_LOGGED_IN` and so always makes the store re-emit.
     */
    var token: String = "first"
        set(value) {
            field = value
            revisions.value++
        }

    var loggedIn: Boolean = true
        set(value) {
            field = value
            revisions.value++
        }

    /** Thrown by the next single read, for the "cannot confirm" path. */
    var readFailure: IOException? = null

    /** How many times the session *flow* fails before it starts working. */
    var sessionFailures: Int = 0

    /** A non-IO failure on the session flow, which must not be retried. */
    var sessionError: Throwable? = null

    private val revisions = MutableStateFlow(0)

    private var tokenGate: CompletableDeferred<Unit>? = null
    private var tokenRead: CompletableDeferred<Unit>? = null

    fun blockTokenRead() {
        tokenGate = CompletableDeferred()
        tokenRead = CompletableDeferred()
    }

    suspend fun awaitTokenRead() = tokenRead?.await() ?: Unit

    fun releaseTokenRead() {
        tokenGate?.complete(Unit)
    }

    override suspend fun getAuthToken(): String {
        readFailure?.let { throw it }
        tokenRead?.complete(Unit)
        tokenGate?.await()
        return token
    }

    override suspend fun isLoggedIn(): Boolean {
        readFailure?.let { throw it }
        return loggedIn
    }

    /**
     * Re-emits on every [revisions] bump — which every `renew` in these tests causes, the way a
     * real `renew` always writes `IS_LOGGED_IN`. Failures are consumed one per subscription, so a
     * retry finds the store working.
     */
    override fun observeHasSession(): Flow<Boolean> = kotlinx.coroutines.flow.flow {
        sessionError?.let { throw it }
        if (sessionFailures > 0) {
            sessionFailures--
            throw IOException("disk")
        }
        revisions.collect { emit(loggedIn && token.isNotBlank()) }
    }

    override suspend fun getStatusLastSyncAt(): Long? = null
    override suspend fun saveStatusLastSyncAt(timestamp: Long) = Unit
    override suspend fun saveAuthToken(token: String) = Unit
    override suspend fun clearAuthToken() = Unit
    override suspend fun setLoggedIn(loggedIn: Boolean) = Unit
    override suspend fun getLanguage(): String? = null
    override fun observeLanguage(): Flow<String?> = flowOf(null)
    override suspend fun saveLanguage(language: String) = Unit
    override fun observeThemeMode(): Flow<String?> = flowOf(null)
    override suspend fun saveThemeMode(mode: String) = Unit
    override suspend fun clearAll() = Unit
}
