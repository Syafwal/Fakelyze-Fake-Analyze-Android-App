package com.wall.fakelyze

import android.app.Application
import android.util.Log
import com.wall.fakelyze.di.appModule
import com.wall.fakelyze.di.repositoryModule
import com.wall.fakelyze.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class FakelyzeApplication : Application() {

    companion object {
        private const val TAG = "FakelyzeApplication"
    }

    override fun onCreate() {
        super.onCreate()

        // Setup global exception handler untuk mencegah crash
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)
            // Log stack trace untuk debugging
            exception.printStackTrace()
        }

        try {
            initializeKoin()
            Log.d(TAG, "Fakelyze Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Critical error initializing application", e)
            // Continue without crashing - app will work with reduced functionality
        }
    }

    private fun initializeKoin() {
        try {
            // Safe Koin initialization with comprehensive error handling
            if (org.koin.core.context.GlobalContext.getOrNull() == null) {
                startKoin {
                    // Use androidLogger only in debug builds to prevent performance issues
                    if (BuildConfig.DEBUG) {
                        androidLogger(Level.DEBUG)
                    }
                    androidContext(this@FakelyzeApplication)
                    modules(
                        appModule,
                        repositoryModule,
                        viewModelModule
                    )
                }
                Log.d(TAG, "Koin initialized successfully")
            } else {
                Log.d(TAG, "Koin already initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Koin", e)
            throw e // Re-throw untuk handling di onCreate
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        cleanup()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning - performing cleanup")
        performMemoryCleanup()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "Memory trim level: $level - cleaning up")
                performMemoryCleanup()
            }
        }
    }

    private fun cleanup() {
        try {
            // Cleanup Koin if needed
            org.koin.core.context.GlobalContext.stopKoin()
            Log.d(TAG, "Application cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    private fun performMemoryCleanup() {
        try {
            // Force garbage collection
            System.gc()
            Log.d(TAG, "Memory cleanup performed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during memory cleanup", e)
        }
    }
}
