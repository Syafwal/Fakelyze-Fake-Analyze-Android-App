package com.wall.fakelyze.data

import android.content.Context
import com.wall.fakelyze.data.database.AppDatabase
import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.data.repository.HistoryRepositoryImpl
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import com.wall.fakelyze.data.repository.UserPreferencesRepositoryImpl
import com.wall.fakelyze.ml.ImageClassifier
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Interface to provide dependencies throughout the app
 * @deprecated Use Koin injection directly instead of this container
 */
@Deprecated("Use Koin injection directly instead", ReplaceWith("inject<T>()"))
interface AppContainer {
    val imageClassifier: ImageClassifier
    val historyRepository: HistoryRepository
    val userPreferencesRepository: UserPreferencesRepository
}

/**
 * Implementation of [AppContainer] providing concrete implementations of dependencies via Koin
 * @deprecated Use Koin injection directly instead of this container
 */
@Deprecated("Use Koin injection directly instead", ReplaceWith("inject<T>()"))
class AppContainerImpl(private val applicationContext: Context) : AppContainer, KoinComponent {

    private val database by inject<AppDatabase>()

    override val imageClassifier by inject<ImageClassifier>()

    override val historyRepository by inject<HistoryRepository>()

    override val userPreferencesRepository by inject<UserPreferencesRepository>()
}

