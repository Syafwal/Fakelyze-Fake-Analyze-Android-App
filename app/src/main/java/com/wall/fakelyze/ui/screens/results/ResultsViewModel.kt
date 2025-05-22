package com.wall.fakelyze.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.domain.model.DetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ResultsViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    // Load detection result
    fun loadDetectionResult(
        imagePath: String,
        thumbnailPath: String,
        isAIGenerated: Boolean,
        confidenceScore: Float
    ) {
        val detectionResult = DetectionResult(
            imagePath = imagePath,
            thumbnailPath = thumbnailPath,
            isAIGenerated = isAIGenerated,
            confidenceScore = confidenceScore,
            timestamp = Date()
        )

        _uiState.value = _uiState.value.copy(
            detectionResult = detectionResult,
            isLoading = false
        )

        saveToHistory(detectionResult)
    }

    // Save result to history
    private fun saveToHistory(detectionResult: DetectionResult) {
        viewModelScope.launch {
            try {
                historyRepository.saveHistory(detectionResult)
                _uiState.value = _uiState.value.copy(
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save to history: ${e.message}"
                )
            }
        }
    }

    // Clear error message after it's shown
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ResultsUiState(
    val detectionResult: DetectionResult? = null,
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)
