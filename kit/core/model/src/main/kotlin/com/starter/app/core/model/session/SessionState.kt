package com.starter.app.core.model.session

/**
 * What the app knows about the stored session — **three answers, not two**.
 *
 * The third one is the whole point. "Could not read the session" is not the same thing as "there is
 * no session", and collapsing them is how a user gets thrown back to the login screen mid-run
 * because a disk read failed for a moment. So a failure surfaces as [Unavailable], and the shell
 * neither navigates nor clears anything on it.
 *
 * Note this is deliberately **not** derived from the user's Room row. That row is data the
 * session happens to have written; it is not evidence a session exists, and reading it as such is
 * what let the shell decide the app was signed out while the splash was still working.
 */
sealed interface SessionState {

    /**
     * A token and the logged-in flag are both stored.
     *
     * **[sessionId] is load-bearing, and not only for telling two users apart.** With a bare
     * `data object` here, a fast logout-then-login gives `Active` followed by `Active` — and
     * `distinctUntilChanged` on the flow swallows the second one entirely, so nothing downstream
     * ever learns the session changed. Carrying the id makes the two values unequal, which is what
     * makes the emission happen at all.
     *
     * It is also what every background task is tagged with: a result that comes back after the id
     * moved belongs to a session that is over, and is dropped rather than applied to the new one.
     */
    data class Active(val sessionId: Long) : SessionState

    /** Confirmed: no token, or the logged-in flag is down. The only state that may sign anyone out. */
    data object SignedOut : SessionState

    /**
     * The store could not be read. Not signed out, and not a licence to keep trusting the last
     * [Active] either — callers should hold off on anything that depends on the session until a
     * real answer arrives.
     */
    data object Unavailable : SessionState
}
