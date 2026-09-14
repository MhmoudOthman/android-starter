package com.starter.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.starter.app.core.datastore.session.SessionGuard
import com.starter.app.core.model.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * `AppDataStoreImpl` over a **real** preferences store on a temporary file.
 *
 * It takes a `DataStore` rather than a `Context` precisely so this is possible on the JVM: no
 * device, no Robolectric, and a test can put whatever it likes on disk first.
 *
 * What is pinned here is the decision that is *ours* rather than DataStore's: **a store with
 * nothing in it reads as a confirmed [SessionState.SignedOut], never as
 * [SessionState.Unavailable]**. That is the difference between the app routing a user to sign in
 * again and it freezing while it waits for a session that will never appear — and it is exactly the
 * state the corruption handler leaves behind, since `ReplaceFileCorruptionHandler` rebuilds the
 * file as `emptyPreferences()`.
 *
 * **One thing here is platform-dependent, and it is marked rather than skipped quietly.** DataStore
 * commits by renaming a `.tmp` over the target, and on Windows `renameTo` fails when the
 * destination already exists — which is every write after the first, and the corruption handler's
 * own replacement write. The corruption test below therefore carries an `assumeFalse` on Windows
 * and runs for real on CI's Linux; everything else in this file reads rather than writes, so it
 * runs everywhere. Ordinary writing behaviour is covered where it belongs: `SessionGuardTest` for
 * the ordering rules, `AuthRepoImplTest` for the commit point.
 */
class AppDataStoreImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    /**
     * A **real** scope, not `runTest`'s.
     *
     * DataStore does its file work on the scope it is built with, and handing it the test's own
     * deadlocks: the test body waits on a read that runs on a virtual clock nobody is advancing,
     * because the body itself is what would advance it. A real IO scope makes the read an ordinary
     * suspension that `runTest` simply waits out.
     */
    private val storeScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * [file] must **not** already exist — `TemporaryFolder.newFile` creates it, and DataStore's
     * first commit then has to rename over a file that is already there, which Windows refuses.
     */
    private fun storeOver(file: File): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        // The same handler `AppDataStoreImpl` installs on the real delegate.
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = storeScope,
        produceFile = { file },
    )

    private fun freshStore(name: String) = AppDataStoreImpl(storeOver(File(folder.root, name)))

    @After
    fun tearDown() {
        storeScope.cancel()
    }

    @Test
    fun `a store with nothing in it has no token, no session and no language`() = runTest {
        val dataStore = freshStore("fresh.preferences_pb")

        assertEquals("", dataStore.getAuthToken())
        assertFalse(dataStore.isLoggedIn())
        assertNull(dataStore.getLanguage())
        assertFalse(dataStore.observeHasSession().first())
    }

    /**
     * The shape the app is left in after the corruption handler has rebuilt the file: the store
     * answers perfectly well, it simply has nothing in it.
     *
     * So the answer is **`SignedOut`, not `Unavailable`** — and that is what lets the defaults be
     * used safely here and nowhere else. They are reached through a *successful* read of an empty
     * store, never substituted for a failure.
     */
    @Test
    fun `an empty store reads as a confirmed sign-out rather than an unreadable one`() = runTest {
        val guard = SessionGuard(freshStore("rebuilt.preferences_pb"))

        assertEquals(SessionState.SignedOut, guard.observeSessionState().first())
        assertNull("nothing may be written for a session that is not there", guard.snapshot())
    }

    /** The language falls back rather than throwing, so an unreadable one cannot fail an orders request. */
    @Test
    fun `an absent language reads as null rather than failing`() = runTest {
        val dataStore = freshStore("language.preferences_pb")

        assertNull(dataStore.getLanguage())
        assertNull(dataStore.observeLanguage().first())
    }

    /**
     * **The corruption path end to end**: a file full of bytes that are not a preferences proto is
     * *replaced* rather than left unreadable, and what comes back is a working store with nothing
     * in it — which the session then reads as a confirmed sign-out.
     *
     * Without the handler a `CorruptionException` is thrown on every read from then on and no retry
     * can help, so the app could never again answer whether anyone is signed in. Rebuilding costs
     * the user a sign-in, which is recoverable; the alternative is not.
     *
     * **Skipped on Windows, and it genuinely runs on CI.** DataStore commits by renaming a `.tmp`
     * over the target, and Windows `renameTo` fails when the destination exists — which the
     * handler's own replacement write always hits, since the corrupt file is by definition already
     * there. CI runs on Linux, where the rename overwrites, so this is real coverage rather than a
     * note in a document; it is simply unobservable on this machine.
     */
    @Test
    fun `a corrupt file is replaced and reads back as a confirmed sign-out`() = runTest {
        assumeFalse(
            "DataStore cannot commit its replacement write on Windows — see the KDoc",
            System.getProperty("os.name").orEmpty().startsWith("Windows"),
        )

        val file = File(folder.root, "corrupt.preferences_pb")
        file.writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07))

        val guard = SessionGuard(AppDataStoreImpl(storeOver(file)))

        assertEquals(SessionState.SignedOut, guard.observeSessionState().first())
        assertNull("a rebuilt file carries no session", guard.snapshot())
    }
}
