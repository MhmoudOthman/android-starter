package com.starter.app.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps `Dispatchers.Main` for a test dispatcher around each test.
 *
 * Every ViewModel here launches on `viewModelScope`, which is `Main`-bound, and `sendEffect` adds an
 * explicit `Dispatchers.Main` on top — without this rule they all fail on the missing main looper.
 *
 * The default is [UnconfinedTestDispatcher] rather than the standard one: these tests assert on
 * what a state looks like *after* an intent, not on the interleaving of the coroutines that got it
 * there, and eager execution means a test reads as `onIntent(...)` then `assert(...)` with no
 * scheduler nudging in between. A test that genuinely cares about ordering can pass a
 * `StandardTestDispatcher` instead and drive it with `advanceUntilIdle()`.
 */
class MainDispatcherRule(
    /**
     * Exposed so a test can hand the *same* dispatcher to `runTest`. Sharing it shares the
     * scheduler, which is what makes effects collected on `backgroundScope` arrive before the
     * assertions rather than one `advanceUntilIdle()` later.
     */
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
