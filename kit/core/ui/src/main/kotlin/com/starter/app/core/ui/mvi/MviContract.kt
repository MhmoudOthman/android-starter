package com.starter.app.core.ui.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MviContract<Intent, State, Effect> {
    val state: StateFlow<State>
    val effect: Flow<Effect>
    fun onIntent(intent: Intent)
}
