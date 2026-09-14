package com.starter.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Koin resolves dependencies at runtime, so a `get()` with nothing registered behind it is a crash
 * when the screen opens — not a compile error. `verify()` walks every definition's constructor and
 * fails here, at test time, if a parameter type has no definition.
 *
 * [extraTypes] lists what the app supplies *outside* Koin: the Android `Context`
 * (`androidContext()`), the `SavedStateHandle` a `viewModel { }` receives from the owner, the
 * `DataStore` built from the Context, and Ktor's engine/config behind `HttpClientFactory.create()`.
 * Add a type here only when a factory function genuinely provides it — never to silence a real gap.
 */
@OptIn(KoinExperimentalAPI::class)
class AppModulesTest {

    @Test
    fun `every Koin definition can be resolved`() {
        module { includes(appModules) }.verify(
            extraTypes = listOf(
                Context::class,
                SavedStateHandle::class,
                DataStore::class,
                HttpClientEngine::class,
                HttpClientConfig::class,
            ),
        )
    }
}
