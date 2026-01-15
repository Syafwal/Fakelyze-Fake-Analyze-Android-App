package com.wall.fakelyze.ui.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.domain.model.DetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                historyRepository.getAllHistory()
                    .catch { exception ->
                        Log.e(TAG, "Error loading history", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Gagal memuat riwayat: ${exception.localizedMessage}"
                        )
                    }
                    .collect { historyList ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            historyList = historyList,
                            error = null
                        )
                        Log.d(TAG, "History loaded successfully: ${historyList.size} items")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading history", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Kesalahan tak terduga: ${e.localizedMessage}"
                )
            }
        }
    }

    fun refreshHistory() {
        Log.d(TAG, "Refreshing history")
        loadHistory()
    }

    fun deleteHistory(historyItem: DetectionResult) {
        viewModelScope.launch {
            try {
                historyRepository.deleteHistory(historyItem)
                Log.d(TAG, "History item deleted: ${historyItem.id}")
                // Refresh list after deletion
                loadHistory()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting history item", e)
                _uiState.value = _uiState.value.copy(
                    error = "Gagal menghapus item: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Clear all history items
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                historyRepository.clearAllHistory()
                Log.d(TAG, "All history cleared")
                _uiState.value = _uiState.value.copy(
                    historyList = emptyList(),
                    error = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing all history", e)
                _uiState.value = _uiState.value.copy(
                    error = "Gagal menghapus semua riwayat: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * PERBAIKAN: Tambahkan fungsi clearError yang hilang
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyList: List<DetectionResult> = emptyList(),
    val error: String? = null
)
