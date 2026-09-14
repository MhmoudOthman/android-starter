package com.starter.app.core.network.di

import com.starter.app.core.network.HttpClientFactory
import com.starter.app.core.network.connectivity.ConnectivityChecker
import com.starter.app.core.network.connectivity.ConnectivityCheckerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create() }

    // Every API surface is an interface bound to its Impl here, one line per endpoint group:
    // single<AuthAPI> { AuthAPIImpl(get()) }

    single<ConnectivityChecker> { ConnectivityCheckerImpl(androidContext()) }
}
