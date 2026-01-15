package com.wall.fakelyze

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import com.wall.fakelyze.ui.AIImageDetectorApp
import com.wall.fakelyze.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val userPreferencesRepository: UserPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()

            setContent {
                AppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var isAppReady by remember { mutableStateOf(false) }
                        var hasError by remember { mutableStateOf(false) }
                        var errorMessage by remember { mutableStateOf("") }

                        // Safe initialization with proper error handling
                        LaunchedEffect(Unit) {
                            try {
                                // Initialize app on IO dispatcher to avoid blocking UI
                                withContext(Dispatchers.IO) {
                                    // Preload critical data
                                    userPreferencesRepository.userPreferencesFlow.collect { prefs ->
                                        Log.d(TAG, "User preferences loaded: $prefs")
                                        // Switch back to Main for UI update
                                        withContext(Dispatchers.Main) {
                                            isAppReady = true
                                        }
                                        return@collect // Stop collecting after first emission
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error during app initialization", e)
                                withContext(Dispatchers.Main) {
                                    hasError = true
                                    errorMessage = "Failed to initialize app: ${e.localizedMessage}"
                                }
                            }
                        }

                        when {
                            hasError -> {
                                // Show error state
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = errorMessage,
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            isAppReady -> {
                                // Show main app
                                AIImageDetectorApp()
                            }
                            else -> {
                                // Show loading state
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Loading...",
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "MainActivity onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Critical error in MainActivity onCreate", e)
            // Show fallback UI or finish activity
            handleCriticalError(e)
        }
    }

    private fun handleCriticalError(exception: Exception) {
        try {
            setContent {
                AppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "App failed to start. Please restart the application.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show error UI", e)
            // Last resort - finish activity
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity onDestroy")
    }
}
