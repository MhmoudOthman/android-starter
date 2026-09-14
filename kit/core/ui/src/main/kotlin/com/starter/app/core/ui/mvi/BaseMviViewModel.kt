package com.starter.app.core.ui.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BaseMviViewModel<Intent, State, Effect>(
    initialState: State,
    protected val savedStateHandle: SavedStateHandle,
) : ViewModel(), MviContract<Intent, State, Effect> {

    abstract val errorHandler: CoroutineExceptionHandler

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<State> = _state.asStateFlow()

    // Channel instead of SharedFlow: effects emitted while no collector is active
    // (e.g. mid-rotation) are buffered and delivered when collection resumes,
    // so one-shot events like navigation are never dropped.
    private val _effect = Channel<Effect>(Channel.BUFFERED)
    override val effect: Flow<Effect> = _effect.receiveAsFlow()

    override fun onIntent(intent: Intent) {}

    protected fun setState(reducer: State.() -> State) {
        _state.update { it.reducer() }
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch(errorHandler + Dispatchers.Main) {
            _effect.send(effect)
        }
    }

    /**
     * Reads a value previously persisted with [saveState], falling back to [default].
     * Survives configuration changes and process death. Call it when building the
     * initial [State]:
     *
     * ```
     * setState { copy(query = savedState(KEY_QUERY, "")) }
     * ```
     */
    protected fun <T : Any> savedState(key: String, default: T): T =
        savedStateHandle[key] ?: default

    /**
     * Persists a small piece of UI state (query text, selected id, scroll position)
     * across configuration changes and process death. Only types supported by
     * [SavedStateHandle] (primitives, String, Parcelable, …) are allowed — never
     * store whole [State] objects or loaded data; reload those instead.
     */
    protected fun <T : Any> saveState(key: String, value: T?) {
        savedStateHandle[key] = value
    }

    /**
     * Observes a persisted value reactively. Useful when the saved value drives
     * work (e.g. re-running a search after restoration).
     */
    protected fun <T : Any> savedStateFlow(key: String, default: T): StateFlow<T> =
        savedStateHandle.getStateFlow(key, default)

    /**
     * Write-through property delegate: every assignment is persisted immediately,
     * so the value can never be lost by forgetting to call [saveState].
     *
     * ```
     * private var query: String by saved("query", "")
     * ```
     */
    protected fun <T : Any> saved(key: String, default: T): ReadWriteProperty<Any?, T> =
        object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T =
                savedStateHandle[key] ?: default

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                savedStateHandle[key] = value
            }
        }
}
