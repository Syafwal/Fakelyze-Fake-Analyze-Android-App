package com.wall.fakelyze.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // UI state derived from user preferences
    val uiState: StateFlow<SettingsUiState> = userPreferencesRepository.userPreferencesFlow
        .map { preferences ->
            SettingsUiState(
                isDarkTheme = preferences?.isDarkTheme ?: false,
                appVersion = "1.0.0" // This could come from BuildConfig in a real app
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    /**
     * Update dark theme preference
     */
    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDarkTheme(enabled)
        }
    }
}

/**
 * Data class representing UI state for settings
 */
data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val appVersion: String = ""
)
