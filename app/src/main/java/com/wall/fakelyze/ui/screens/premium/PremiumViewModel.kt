package com.wall.fakelyze.ui.screens.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.model.PremiumPlan
import com.wall.fakelyze.data.model.PremiumStatus
import com.wall.fakelyze.data.model.ScanLimitStatus
import com.wall.fakelyze.data.model.UserRole
import com.wall.fakelyze.data.model.UserRoleInfo
import com.wall.fakelyze.data.repository.PremiumRepository
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PremiumUiState(
    val premiumStatus: PremiumStatus = PremiumStatus(),
    val availablePlans: List<PremiumPlan> = emptyList(),
    val scanLimitStatus: ScanLimitStatus = ScanLimitStatus(canScan = true, remainingScans = 10, isUnlimited = false),
    val userRole: UserRole = UserRole.FREE,
    val userRoleInfo: UserRoleInfo = UserRoleInfo.getInfoForRole(UserRole.FREE),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PremiumViewModel(
    private val premiumRepository: PremiumRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        loadPremiumData()
        observePremiumStatusAndRole()
    }

    private fun loadPremiumData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val plans = premiumRepository.getAvailablePlans()
                val premiumStatus = premiumRepository.getPremiumStatus()
                val scanLimitStatus = premiumRepository.getScanLimitStatus()

                _uiState.value = _uiState.value.copy(
                    availablePlans = plans,
                    premiumStatus = premiumStatus,
                    scanLimitStatus = scanLimitStatus,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun observePremiumStatusAndRole() {
        viewModelScope.launch {
            combine(
                premiumRepository.premiumStatusFlow,
                userPreferencesRepository.userPreferencesFlow
            ) { premiumStatus: PremiumStatus, userPreferences ->
                val userRole = userPreferences.userRole
                val userRoleInfo = UserRoleInfo.getInfoForRole(userRole)
                val scanLimitStatus = premiumRepository.getScanLimitStatus()

                _uiState.value = _uiState.value.copy(
                    premiumStatus = premiumStatus,
                    userRole = userRole,
                    userRoleInfo = userRoleInfo,
                    scanLimitStatus = scanLimitStatus
                )
            }.collect { }
        }
    }

    fun purchasePlan(plan: PremiumPlan) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Simulate payment process
                val success = premiumRepository.purchasePlan(plan)

                if (success) {
                    // Update user role to premium
                    userPreferencesRepository.updateUserRole(UserRole.PREMIUM)

                    // Refresh premium status
                    loadPremiumData()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gagal memproses pembayaran. Silakan coba lagi."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Terjadi kesalahan saat memproses pembayaran"
                )
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val restored = premiumRepository.restorePurchases()

                if (restored) {
                    loadPremiumData()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Tidak ada pembelian yang dapat dipulihkan"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Gagal memulihkan pembelian"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun changePlan(newPlan: PremiumPlan) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val success = premiumRepository.changePlan(newPlan)

                if (success) {
                    loadPremiumData()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gagal mengubah paket. Silakan coba lagi."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Terjadi kesalahan saat mengubah paket"
                )
            }
        }
    }

    fun downgradeToFree() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val success = premiumRepository.cancelSubscription()

                if (success) {
                    userPreferencesRepository.updateUserRole(UserRole.FREE)
                    loadPremiumData()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gagal membatalkan langganan. Silakan coba lagi."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Terjadi kesalahan saat membatalkan langganan"
                )
            }
        }
    }

    fun checkScanLimit(): Boolean {
        val currentState = _uiState.value
        return currentState.scanLimitStatus.canScan
    }

    fun getRemainingScans(): Int {
        return _uiState.value.scanLimitStatus.remainingScans
    }

    fun isUnlimited(): Boolean {
        return _uiState.value.scanLimitStatus.isUnlimited
    }
}

class PremiumViewModelFactory(
    private val premiumRepository: PremiumRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PremiumViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PremiumViewModel(premiumRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
