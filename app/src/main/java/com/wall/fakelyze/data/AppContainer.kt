package com.wall.fakelyze.data

import android.content.Context
import androidx.room.Room
import com.wall.fakelyze.data.database.AppDatabase
import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.data.repository.HistoryRepositoryImpl
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import com.wall.fakelyze.data.repository.UserPreferencesRepositoryImpl
import com.wall.fakelyze.ml.ImageClassifier

/**
 * Interface to provide dependencies throughout the app
 */
interface AppContainer {
    val imageClassifier: ImageClassifier
    val historyRepository: HistoryRepository
    val userPreferencesRepository: UserPreferencesRepository
}

/**
 * Implementation of [AppContainer] providing concrete implementations of dependencies
 */
class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ai_image_detector_database"
        ).build()
    }

    override val imageClassifier: ImageClassifier by lazy {
        ImageClassifier(applicationContext)
    }

    override val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(database.historyDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(applicationContext)
    }
}