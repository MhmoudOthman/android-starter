package com.starter.app.core.ui.component.textinputs

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A [TextFieldState] the screen owns, reporting every edit to [onTextChange] as a plain `String`.
 *
 * The bridge between Compose's state-based text fields and an MVI ViewModel that keeps its text in
 * `State` as `String`: the field edits its own state (cursor, selection, IME composition — the
 * things that went wrong when a value round-tripped through a ViewModel on every keystroke), and
 * the ViewModel hears the result through an intent. This is the pattern the deprecation notice
 * itself points to: "observe text changes for side effects via snapshotFlow".
 *
 * - [initialText] seeds the field once — usually the ViewModel's current value, so a field the
 *   ViewModel already holds text for opens with it.
 * - The state is saveable, so it survives a recreate (a language or theme change) and process
 *   death. On restore it reports its text again; a ViewModel should therefore treat an intent that
 *   carries the text it already has as a no-op — not, say, as a reason to clear that field's error.
 *
 * Text flows one way, field → ViewModel. A ViewModel that needs to *set* the text (clear a form,
 * fill a scanned code) should own the [TextFieldState] itself instead.
 */
@Composable
fun rememberForwardingTextFieldState(
    initialText: String = "",
    onTextChange: (String) -> Unit,
): TextFieldState {
    val state = rememberTextFieldState(initialText)
    val currentOnTextChange by rememberUpdatedState(onTextChange)
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .distinctUntilChanged()
            .collect { currentOnTextChange(it) }
    }
    return state
}
