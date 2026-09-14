package com.starter.app.core.datastore.di

import com.starter.app.core.datastore.AppDataStore
import com.starter.app.core.datastore.AppDataStoreImpl
import com.starter.app.core.datastore.appPreferences
import com.starter.app.core.datastore.session.SessionGuard
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataStoreModule = module {
    // Bound to the interface: every consumer depends on AppDataStore, which is what makes them
    // testable without a Context.
    single<AppDataStore> { AppDataStoreImpl(androidContext().appPreferences) }

    // A `single` because the session id it holds is process-wide state: a second instance would
    // keep its own counter, and the auth repo bumping one would leave the profile repo comparing
    // against the other.
    single { SessionGuard(get()) }
}
