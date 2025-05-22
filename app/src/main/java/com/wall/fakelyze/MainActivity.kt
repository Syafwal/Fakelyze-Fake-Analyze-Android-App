package com.wall.fakelyze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wall.fakelyze.ui.AIImageDetectorApp
import com.wall.fakelyze.ui.theme.FakelyzeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appContainer = (application as AIImageDetectorApp).container
            val userPreferences = appContainer.userPreferencesRepository
            val isDarkTheme by userPreferences.userPreferencesFlow.collectAsState(initial = null)

            FakelyzeTheme(
                darkTheme = isDarkTheme?.isDarkTheme ?: isSystemInDarkTheme()
            ) {
                AIImageDetectorApp(
                    appContainer = appContainer
                )
            }
        }
    }
}