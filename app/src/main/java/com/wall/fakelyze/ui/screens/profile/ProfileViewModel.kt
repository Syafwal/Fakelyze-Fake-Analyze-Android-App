package com.wall.fakelyze.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import com.wall.fakelyze.data.repository.PremiumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val premiumRepository: PremiumRepository,
    private val historyRepository: com.wall.fakelyze.data.repository.HistoryRepository
) : ViewModel() {

    // PERBAIKAN: UI state yang lebih sederhana tanpa konflik logika
    val uiState: StateFlow<ProfileUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        premiumRepository.premiumStatusFlow
    ) { preferences, premiumStatus ->
        // PERBAIKAN: Logika yang sangat sederhana untuk remaining scans
        val remainingScans = if (premiumStatus.isPremium) {
            -1 // Unlimited untuk premium
        } else {
            premiumStatus.remainingScans
        }

        // PERBAIKAN: Log untuk debugging
        android.util.Log.d(TAG, "ProfileViewModel - isPremium: ${premiumStatus.isPremium}, remaining: $remainingScans")

        ProfileUiState(
            username = preferences.username,
            displayName = preferences.displayName.ifEmpty { "User" },
            email = preferences.email.ifEmpty { "user@example.com" },
            phone = preferences.phone.ifEmpty { "-" },
            role = if (premiumStatus.isPremium) "Premium" else "Free",
            scanCount = 0, // Will be updated via combinedUiState
            aiDetectedCount = 0, // Will be updated via combinedUiState
            realImageCount = 0, // Will be updated via combinedUiState
            isDarkTheme = preferences.isDarkTheme,
            profilePhotoUri = preferences.profilePhotoUri?.let { Uri.parse(it) },
            isPremium = premiumStatus.isPremium,
            remainingScans = remainingScans,
            premiumPlanId = premiumStatus.planId,
            premiumExpiryDate = premiumStatus.expiryDate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    // PERBAIKAN: Load history statistics separately on background thread
    private val _historyStats = MutableStateFlow(HistoryStats())

    init {
        loadHistoryStats()
    }

    private fun loadHistoryStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepository.getAllHistory().collect { historyList ->
                    val stats = HistoryStats(
                        totalScans = historyList.size,
                        aiDetectedCount = historyList.count { it.isAIGenerated },
                        realImageCount = historyList.count { !it.isAIGenerated }
                    )
                    _historyStats.value = stats

                    // PERBAIKAN: Log untuk debugging
                    android.util.Log.d(TAG, "History stats updated - total: ${stats.totalScans}, AI: ${stats.aiDetectedCount}, Real: ${stats.realImageCount}")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading history stats", e)
            }
        }
    }

    // PERBAIKAN: Combined state that includes history stats
    val combinedUiState: StateFlow<ProfileUiState> = combine(
        uiState,
        _historyStats
    ) { profileState, historyStats ->
        profileState.copy(
            scanCount = historyStats.totalScans,
            aiDetectedCount = historyStats.aiDetectedCount,
            realImageCount = historyStats.realImageCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    /**
     * Update user profile data
     */
    fun updateUserProfile(username: String, displayName: String, email: String, phone: String) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateUserProfile(username, displayName, email, phone)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user profile", e)
            }
        }
    }

    /**
     * Update profile photo URI
     */
    fun updateProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateProfilePhoto(uri.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile photo", e)
            }
        }
    }

    /**
     * Update dark theme preference
     */
    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateDarkTheme(enabled)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating dark theme", e)
            }
        }
    }

    /**
     * Upgrade to premium plan
     */
    fun upgradeToPremium(planId: String = "premium_monthly") {
        viewModelScope.launch {
            try {
                // Upgrade ke premium dengan plan ID
                premiumRepository.upgradeToPremium(planId)

                // Sync role dengan user preferences
                userPreferencesRepository.syncRoleWithPremiumStatus(true)

                Log.d(TAG, "Successfully upgraded to premium with plan: $planId")
            } catch (e: Exception) {
                Log.e(TAG, "Error upgrading to premium", e)
            }
        }
    }

    /**
     * Downgrade from premium to free user
     */
    fun downgradeToPremium() {
        viewModelScope.launch {
            try {
                // Cancel subscription to downgrade to free user
                premiumRepository.cancelSubscription()

                // Sync role dengan user preferences
                userPreferencesRepository.syncRoleWithPremiumStatus(false)

                Log.d(TAG, "Successfully downgraded to free")
            } catch (e: Exception) {
                Log.e(TAG, "Error downgrading to free", e)
            }
        }
    }

    /**
     * Logout function to clear user data and preferences
     * @param onLogoutComplete callback dipanggil setelah logout selesai
     */
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.clearUserData()
                onLogoutComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
                onLogoutComplete() // Still call completion even on error
            }
        }
    }

    /**
     * Increment scan count
     */
    fun incrementScanCount() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.incrementScanCount()
            } catch (e: Exception) {
                Log.e(TAG, "Error incrementing scan count", e)
            }
        }
    }

    /**
     * Increment AI detected count
     */
    fun incrementAiDetectedCount() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.incrementAiDetectedCount()
            } catch (e: Exception) {
                Log.e(TAG, "Error incrementing AI detected count", e)
            }
        }
    }

    /**
     * Increment real image count
     */
    fun incrementRealImageCount() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.incrementRealImageCount()
            } catch (e: Exception) {
                Log.e(TAG, "Error incrementing real image count", e)
            }
        }
    }

    /**
     * Cek apakah user bisa melakukan scan dan kurangi count jika user gratis
     * @return true jika scan diizinkan, false jika limit tercapai
     */
    suspend fun canScanAndDecrement(): Boolean {
        return try {
            premiumRepository.decrementScanCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking scan limit", e)
            false
        }
    }

    /**
     * Cek apakah user bisa melakukan scan tanpa mengurangi count
     * @return true jika scan diizinkan
     */
    suspend fun canScan(): Boolean {
        return try {
            premiumRepository.canScanToday()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking scan availability", e)
            false
        }
    }

    /**
     * Dapatkan status scan limit untuk UI
     */
    suspend fun getScanLimitStatus(): com.wall.fakelyze.data.model.ScanLimitStatus {
        return try {
            premiumRepository.getScanLimitStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting scan limit status", e)
            com.wall.fakelyze.data.model.ScanLimitStatus(
                canScan = false,
                remainingScans = 0,
                isUnlimited = false,
                resetTime = null,
                message = "Tidak dapat mengakses informasi batas scan. Silakan coba lagi."
            )
        }
    }

    /**
     * Refresh history statistics manually
     */
    fun refreshHistoryStats() {
        loadHistoryStats()
    }

    /**
     * Update statistics when a new scan is completed
     * This should be called after saving scan result to history
     */
    fun updateStatsAfterScan(isAIGenerated: Boolean) {
        viewModelScope.launch {
            try {
                // Refresh stats setelah scan baru
                refreshHistoryStats()

                // Log untuk debugging
                android.util.Log.d(TAG, "Stats updated after scan - AI Generated: $isAIGenerated")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error updating stats after scan", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProfileViewModel cleared")
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}

/**
 * Data class representing UI state for profile
 */
data class ProfileUiState(
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val scanCount: Int = 0,
    val aiDetectedCount: Int = 0,
    val realImageCount: Int = 0,
    val isDarkTheme: Boolean = false,
    val profilePhotoUri: Uri? = null,
    // Premium-related fields
    val isPremium: Boolean = false,
    val remainingScans: Int = 0,
    val premiumPlanId: String? = null,
    val premiumExpiryDate: Long? = null
)

/**
 * Data class representing history statistics
 */
data class HistoryStats(
    val totalScans: Int = 0,
    val aiDetectedCount: Int = 0,
    val realImageCount: Int = 0
)
