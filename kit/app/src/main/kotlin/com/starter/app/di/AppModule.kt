package com.starter.app.di

import com.starter.app.launch.LaunchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** What `:app` itself defines: the launch decision behind the system splash. */
val appModule = module {
    viewModelOf(::LaunchViewModel)
}
