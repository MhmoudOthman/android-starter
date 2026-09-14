package com.starter.app.di

import com.starter.app.core.datastore.di.dataStoreModule
import com.starter.app.core.network.di.networkModule
import org.koin.core.module.Module

/**
 * The ONLY place Koin modules are aggregated. There is no central `:core:di` module on purpose:
 * each module owns its own `module { }` in a local `di` package, and this list references them.
 *
 * Order: core → domain:common/data:common → each feature's domain, data, presentation → shell.
 */
val appModules: List<Module> = listOf(
    networkModule,
    dataStoreModule,
    // databaseModule,
    // domainCommonModule, dataCommonModule,
    // domainAuthModule, dataAuthModule, presentationLoginModule,
    // presentationShellModule,
)
