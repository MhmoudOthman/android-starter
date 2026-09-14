package com.starter.app.core.datastore.session

import com.starter.app.core.datastore.AppDataStore
import com.starter.app.core.datastore.backoffMillis
import com.starter.app.core.model.session.SessionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

/**
 * The session as one request saw it when it left. Carried across the network call and handed back
 * to [SessionGuard.ifStillCurrent] at write time, so a reply can be matched against the session
 * that asked for it rather than against whatever is current when it lands.
 */
data class SessionSnapshot(val id: Long, val token: String)

/**
 * The single owner of "is there a session, and which one".
 *
 * Everything that reads or ends a session goes through here, for one reason: a request and the
 * write it produces are separated by a full network round trip, and the session can end — or be
 * replaced by another user's — in the middle of it. Without a guard, a reply for account A lands
 * on account B's row, and a `GET profile` that comes back after a logout writes the user back
 * into a database that was just cleared.
 *
 * Three rules hold everywhere in this class, and each of them costs something when broken:
 *
 * - **The lock covers local storage only.** Room and DataStore, nothing else. A network call inside
 *   [ifStillCurrent] or [renew] would hold the lock for the length of a request and deadlock every
 *   other session operation behind it.
 * - **Validity is re-read, never inferred from the id alone.** The id moves on login and logout,
 *   but DataStore's corruption handler can replace the whole preferences file without passing
 *   through either — so an id comparison would still say "same session" over a token that no
 *   longer exists. Reading the current truth covers that case with no extra machinery, and with no
 *   "invalidation event" that could arrive late and cancel a session that had already started.
 * - **An unreadable store is not an ended session.** [readValidSession] lets an `IOException` reach
 *   the caller, which skips its write and changes nothing else: no clearing, no id bump, no delete.
 *   Failing to confirm is not confirming a failure.
 *
 * Cancellation needs no special handling. `withLock` is cancellable while waiting, and a write that
 * is dropped because its coroutine was cancelled is exactly the outcome wanted.
 */
class SessionGuard(private val dataStore: AppDataStore) {

    private val mutex = Mutex()

    /**
     * Bumped inside [renew] on every login and logout, so a snapshot taken before one can be told
     * from the session after it.
     *
     * In memory on purpose. Process death resets it to zero, but it also takes every in-flight
     * request with it, so there is no reply left that could be matched against a stale id — and
     * persisting it would add a disk write to every sign-in for nothing.
     *
     * `@Volatile` rather than lock-guarded, because [observeSessionState] reads it on every
     * emission: the bump happens under the lock and before the preference write that triggers that
     * emission, so a reader always sees the new value by the time it matters, and taking the lock
     * on a hot flow would queue emissions behind whatever write is in progress.
     */
    @Volatile
    private var sessionId: Long = 0L

    /**
     * The stored session as a stream, for anything that has to react to signing in and out.
     *
     * **The flow never completes.** A read failure emits [SessionState.Unavailable] and then
     * re-subscribes with a capped backoff, rather than falling back to a value or ending. Both of
     * the obvious alternatives are wrong: a terminal `catch` would leave the collector on a stale
     * answer for the life of the subscription, with no way back even once the store recovered, and
     * emitting `SignedOut` on failure would make a momentary disk error indistinguishable from a
     * logout.
     *
     * `retryWhen`'s lambda is a `FlowCollector`, which is what lets the failure be *reported* and
     * retried in the same operator — no second subscription mechanism is needed.
     */
    fun observeSessionState(): Flow<SessionState> = dataStore.observeHasSession()
        .map { hasSession ->
            if (hasSession) SessionState.Active(sessionId) else SessionState.SignedOut
        }
        .retryWhen { cause, attempt ->
            // Anything that is not a storage failure is a programming error and has to surface.
            if (cause !is IOException) return@retryWhen false
            emit(SessionState.Unavailable)
            delay(backoffMillis(attempt))
            true
        }
        .distinctUntilChanged()

    // Note this cannot use `retryOnReadFailure`: that one is deliberately silent, and the session
    // is the one flow that has to *say* it cannot answer rather than going quiet on a stale value.

    /** The session as it stands, or `null` when there is none. Reads both fields under one lock. */
    suspend fun snapshot(): SessionSnapshot? = mutex.withLock { readValidSession() }

    /**
     * The session identity **as it stands right now**, with no lock and no storage read.
     *
     * This is what a caller compares against to decide whether a result it is holding still belongs
     * to the session that asked for it. It has to come from here rather than from a copy the caller
     * kept: [observeSessionState] reaches a collector asynchronously, so between the id moving and
     * the collector hearing about it there is a window where the caller's own copy is stale — and a
     * stale copy matches a stale result, which is the whole thing being guarded against.
     *
     * A plain `@Volatile` read is enough, and is the point: lag-free by construction, and the id is
     * all that is being asked. Whether the *token* is still valid is [ifStillCurrent]'s question,
     * and it is asked under the lock, where a write actually happens.
     */
    fun currentSessionId(): Long = sessionId

    /**
     * Runs [work] only if the session [expected] belongs to is still the current one, with the
     * check and the work under a single lock so a logout cannot slip between them.
     *
     * Returns `null` when the session has moved on and [work] was skipped — callers report that as
     * an ended session rather than as a success, since the data it would have written was thrown
     * away.
     */
    suspend fun <T> ifStillCurrent(expected: SessionSnapshot, work: suspend () -> T): T? =
        mutex.withLock {
            val now = readValidSession()
            if (now?.id == expected.id && now.token == expected.token) work() else null
        }

    /**
     * Runs [work] only if there is still no session, and says whether it did.
     *
     * This is what makes clearing leftover session data safe to retry: an attempt scheduled while
     * signed out but arriving after someone has signed back in finds a live session under the lock
     * and does nothing, so a late cleanup can never delete a new account's data.
     */
    suspend fun ifStillSignedOut(work: suspend () -> Unit): Boolean = mutex.withLock {
        val signedOut = readValidSession() == null
        if (signedOut) work()
        signedOut
    }

    /**
     * Starts or ends a session: the id moves first, then [work] does the storage writes.
     *
     * The id is bumped **before** anything is written so that a failure part-way through still
     * leaves every in-flight reply rejected — the conservative direction. Atomicity across Room and
     * DataStore is not on offer (they are separate stores with no shared transaction), so callers
     * order their writes around a single commit point instead; see `AuthRepoImpl`.
     */
    suspend fun renew(work: suspend () -> Unit) = mutex.withLock {
        sessionId++
        work()
    }

    /**
     * Lets an `IOException` propagate on purpose — see the class doc. A caller that cannot read the
     * session must skip its write, not assume the session is gone.
     */
    private suspend fun readValidSession(): SessionSnapshot? {
        val token = dataStore.getAuthToken()
        return if (token.isNotBlank() && dataStore.isLoggedIn()) {
            SessionSnapshot(sessionId, token)
        } else {
            null
        }
    }

}
