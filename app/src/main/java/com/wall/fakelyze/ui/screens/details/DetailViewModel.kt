package com.wall.fakelyze.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.domain.model.DetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Detection result
    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult.asStateFlow()

    /**
     * Load the detection result from the repository
     */
    fun loadDetectionResult(detectionResultId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val result = historyRepository.getHistoryById(detectionResultId)
                if (result != null) {
                    _detectionResult.value = result
                    _uiState.value = DetailUiState.Success
                } else {
                    _uiState.value = DetailUiState.Error("Detection result not found")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Failed to load detection result: ${e.message}")
            }
        }
    }

    /**
     * Delete the detection result from the repository
     */
    suspend fun deleteDetectionResult(): Boolean {
        return try {
            _detectionResult.value?.let {
                historyRepository.deleteHistory(it)
                true
            } ?: false
        } catch (e: Exception) {
            _uiState.value = DetailUiState.Error("Failed to delete detection result: ${e.message}")
            false
        }
    }
}

/**
 * States for the Detail screen
 */
sealed class DetailUiState {
    data object Loading : DetailUiState()
    data object Success : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}
