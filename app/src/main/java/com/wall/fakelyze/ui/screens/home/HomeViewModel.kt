package com.wall.fakelyze.ui.screens.home

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.ml.ClassificationResult
import com.wall.fakelyze.ml.ImageClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val imageClassifier: ImageClassifier
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Tab selection
    fun setSelectedTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
    }

    // Process the captured or selected image
    fun processImage(bitmap: Bitmap, onResult: (ClassificationResult) -> Unit) {
        _uiState.value = _uiState.value.copy(isProcessing = true)

        viewModelScope.launch {
            try {
                val result = imageClassifier.classifyImage(bitmap)
                onResult(result)
            } catch (e: Exception) {
                onResult(ClassificationResult.Error(e.message ?: "Unknown error occurred"))
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }
}

// Data class representing the UI state
data class HomeUiState(
    val selectedTabIndex: Int = 0,
    val isProcessing: Boolean = false,
    val error: String? = null
)
