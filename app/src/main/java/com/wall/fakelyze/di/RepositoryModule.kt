package com.wall.fakelyze.di

import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.data.repository.HistoryRepositoryImpl
import com.wall.fakelyze.data.repository.PremiumRepository
import com.wall.fakelyze.data.repository.PremiumRepositoryImpl
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import com.wall.fakelyze.data.repository.UserPreferencesRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    // UserPreferencesRepository dengan error handling dan parameter yang benar
    single<UserPreferencesRepository> {
        try {
            UserPreferencesRepositoryImpl(context = androidContext())
        } catch (e: Exception) {
            android.util.Log.e("RepositoryModule", "Failed to create UserPreferencesRepository", e)
            throw IllegalStateException("Critical: UserPreferencesRepository failed to initialize", e)
        }
    }

    // PremiumRepository dengan error handling dan parameter yang benar
    single<PremiumRepository> {
        try {
            PremiumRepositoryImpl(context = androidContext())
        } catch (e: Exception) {
            android.util.Log.e("RepositoryModule", "Failed to create PremiumRepository", e)
            throw IllegalStateException("Critical: PremiumRepository failed to initialize", e)
        }
    }

    // HistoryRepository dengan error handling
    single<HistoryRepository> {
        try {
            HistoryRepositoryImpl(historyDao = get())
        } catch (e: Exception) {
            android.util.Log.e("RepositoryModule", "Failed to create HistoryRepository", e)
            throw IllegalStateException("Critical: HistoryRepository failed to initialize", e)
        }
    }
}
