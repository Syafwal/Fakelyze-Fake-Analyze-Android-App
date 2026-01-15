package com.wall.fakelyze.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.wall.fakelyze.data.database.AppDatabase
import com.wall.fakelyze.data.database.dao.HistoryDao
import com.wall.fakelyze.ml.ImageClassifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Koin module for application dependencies with performance optimizations
 */
val appModule = module {
    // Database dengan proper migration dan threading configuration
    single<AppDatabase> {
        try {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "fakelyze_database"
            )
            .addMigrations(AppDatabase.MIGRATION_1_2) // Gunakan migration yang sudah ada
            .fallbackToDestructiveMigration() // PERBAIKAN: Safe fallback untuk migration issues
            .enableMultiInstanceInvalidation()
            // PERBAIKAN: Tambahkan konfigurasi threading yang tepat
            .setQueryCallback(
                { sqlQuery, bindArgs ->
                    android.util.Log.d("Database", "SQL Query: $sqlQuery")
                },
                java.util.concurrent.Executors.newSingleThreadExecutor()
            )
            // PERBAIKAN: Set executor untuk background operations
            .setQueryExecutor(java.util.concurrent.Executors.newFixedThreadPool(4))
            .setTransactionExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
            .build()
        } catch (e: Exception) {
            android.util.Log.e("AppModule", "Database creation error", e)
            // PERBAIKAN: Return database dengan konfigurasi minimal jika error
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "fakelyze_database_fallback"
            )
            .fallbackToDestructiveMigration()
            // PERBAIKAN CRITICAL: Untuk development saja - hapus di production
            .allowMainThreadQueries() // Temporary fix untuk debugging
            .build()
        }
    }

    // DAO dengan error handling yang lebih robust
    single<HistoryDao> {
        try {
            get<AppDatabase>().historyDao()
        } catch (e: Exception) {
            android.util.Log.e("AppModule", "DAO creation error", e)
            throw IllegalStateException("Critical: HistoryDao failed to initialize", e)
        }
    }

    // Image Classifier dengan proper error handling dan safe initialization
    factory<ImageClassifier> {
        try {
            ImageClassifier(context = androidContext())
        } catch (e: Exception) {
            android.util.Log.e("AppModule", "ImageClassifier creation error", e)
            // Jika gagal, tetap return ImageClassifier yang akan handle error secara internal
            // ImageClassifier sudah memiliki built-in error handling dan safe defaults
            ImageClassifier(context = androidContext().applicationContext)
        }
    }

    single<DataStore<Preferences>> {
        try {
            androidContext().dataStore
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize DataStore", e)
        }
    }
}
