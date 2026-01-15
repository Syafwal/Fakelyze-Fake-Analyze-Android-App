package com.wall.fakelyze.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Shared DataStore instance untuk user preferences
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

// Shared DataStore instance untuk premium preferences
val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "premium_preferences")
