package com.wall.fakelyze

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.wall.fakelyze.data.AppContainer
import com.wall.fakelyze.data.AppContainerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AIImageDetectorApp : Application() {
    lateinit var container: AppContainer

    // For theme setting across the app
    val isDarkTheme = mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(AIImageDetectorApp(this))
        }
        container = AppContainerImpl(this)
    }
}