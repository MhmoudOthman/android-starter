package com.starter.app

import android.app.Application
import com.starter.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // When the app grows a theme setting, its cached night mode is put back here, before
        // startKoin and before any activity exists: AppCompat's night mode lives in memory only, so
        // without it every cold start draws its first frame in the handset's mode (CLAUDE.md §8).
        // The language needs nothing here — the platform (or AppCompat's own store) keeps it.

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AppApplication)
            modules(appModules)
        }
    }
}
