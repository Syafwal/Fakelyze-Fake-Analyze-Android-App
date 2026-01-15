package com.wall.fakelyze

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.wall.fakelyze.di.koinModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AIImageDetectorApp : Application() {
    // For theme setting across the app
    val isDarkTheme = mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin for dependency injection
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AIImageDetectorApp)
            modules(koinModules)
        }
    }
}

