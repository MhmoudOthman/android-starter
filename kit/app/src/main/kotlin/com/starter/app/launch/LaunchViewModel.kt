package com.starter.app.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starter.app.core.datastore.session.SessionGuard
import com.starter.app.core.model.session.SessionState
import com.starter.app.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Decides the first screen while the system splash is still up (`setKeepOnScreenCondition`), so
 * the app opens straight on its first real screen — no second, in-app splash after the system's.
 *
 * The session is read first. `Unavailable` (the store could not be read *yet*) is waited out, never
 * taken as "signed out" — a disk hiccup must not look like a logout.
 *
 * Grows with the app (CLAUDE.md §8):
 * - an auth feature: `SignedOut` opens `Screen.Login` instead of Home, and the session is read
 *   through `domain:common`'s `ObserveSessionUseCase` rather than [SessionGuard] directly;
 * - a language/theme setting: its `ApplyStoredAppearanceUseCase` runs here before the session is
 *   read, inside a try/catch — failing to apply a preference is not a reason to keep anyone on the
 *   splash; the app opens in the handset's look instead.
 *
 * Lives on the activity's ViewModelStore, so a recreate (a language change) does not decide again.
 */
class LaunchViewModel(
    private val sessionGuard: SessionGuard,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Screen?>(null)

    /** `null` while deciding; the splash stays on screen until it is not. */
    val startDestination: StateFlow<Screen?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val session = sessionGuard.observeSessionState().first { it !is SessionState.Unavailable }
            _startDestination.value = when (session) {
                // The kit has no sign-in screen yet, so every answer opens Home.
                is SessionState.Active, SessionState.SignedOut, SessionState.Unavailable -> Screen.Home
            }
        }
    }
}
