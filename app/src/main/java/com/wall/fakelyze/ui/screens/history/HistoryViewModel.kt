package com.wall.fakelyze.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    // UI state holding the history data
    val historyItems = historyRepository.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // SnackBar message state
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    /**
     * Clear all history items
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            _snackbarMessage.value = "History cleared"
        }
    }

    /**
     * Reset snackbar message after it's shown
     */
    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }
}
