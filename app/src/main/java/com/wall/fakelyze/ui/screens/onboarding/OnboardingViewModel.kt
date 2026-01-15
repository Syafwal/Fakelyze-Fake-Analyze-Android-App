package com.wall.fakelyze.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk halaman onboarding
 */
class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /**
     * Menandai bahwa onboarding telah selesai
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState(isCompleted = true)
            // Di sini Anda dapat menambahkan kode untuk menyimpan status onboarding
            // ke dalam DataStore atau SharedPreferences
        }
    }
}

/**
 * Representasi state UI untuk onboarding
 */
data class OnboardingUiState(
    val isCompleted: Boolean = false
)
