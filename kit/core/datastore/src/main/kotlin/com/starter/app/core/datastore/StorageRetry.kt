package com.starter.app.core.datastore

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException

/**
 * How long to wait before re-reading storage after the [attempt]-th failure: doubling from
 * [INITIAL_BACKOFF_MS] up to a [MAX_BACKOFF_MS] ceiling.
 *
 * Retries are unbounded but the wait is not, which is the combination that matters. Giving up
 * entirely would leave a collector stranded on a stale answer with no way back once the disk
 * recovered; retrying at full speed would spin against a store that is failing for a reason.
 *
 * The shift is clamped so a long-lived subscription cannot overflow it into a negative delay.
 */
internal fun backoffMillis(attempt: Long): Long =
    (INITIAL_BACKOFF_MS shl attempt.coerceIn(0, 16).toInt()).coerceAtMost(MAX_BACKOFF_MS)

private const val INITIAL_BACKOFF_MS = 200L
private const val MAX_BACKOFF_MS = 30_000L

/**
 * Re-subscribes after a failed storage read instead of ending, **and emits nothing in between**.
 *
 * The distinction is the whole point. `catch { emit(default) }` looks like the obvious answer and is
 * wrong twice over: it ends the flow, so the collector is stuck on the fallback for the life of the
 * subscription even after the store recovers — and it makes a momentary disk failure
 * indistinguishable from a preference the driver actually chose. `retryWhen` re-collects upstream,
 * so a recovered read reaches the collector on its own.
 *
 * Callers that need to *say* something while the store is unreadable — the session does, since
 * "cannot read" must not be shown as "signed out" — add their own emission; see
 * `SessionGuard.observeSessionState`.
 *
 * Anything that is not an `IOException` is a programming error and is rethrown. `retryWhen` is
 * transparent to cancellation by design, so neither is swallowed here.
 */
internal fun <T> Flow<T>.retryOnReadFailure(): Flow<T> = retryWhen { cause, attempt ->
    if (cause !is IOException) return@retryWhen false
    delay(backoffMillis(attempt))
    true
}
