package com.wall.fakelyze.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wall.fakelyze.data.datastore.premiumDataStore
import com.wall.fakelyze.data.model.PremiumPlan
import com.wall.fakelyze.data.model.PremiumStatus
import com.wall.fakelyze.data.model.ScanLimitStatus
import com.wall.fakelyze.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

interface PremiumRepository {
    val premiumStatusFlow: Flow<PremiumStatus>
    suspend fun upgradeToPremium(planId: String)
    suspend fun updateRemainingScans(count: Int)
    suspend fun decrementScanCount(): Boolean
    suspend fun resetDailyScans()
    suspend fun canScanToday(): Boolean
    suspend fun getRemainingScansCount(): Int
    suspend fun getUserRole(): UserRole
    suspend fun syncUserRole(userPreferencesRepository: UserPreferencesRepository?)
    fun getAvailablePlans(): List<PremiumPlan>
    suspend fun purchasePlan(plan: PremiumPlan): Boolean
    suspend fun getPremiumStatus(): PremiumStatus
    suspend fun getScanLimitStatus(): ScanLimitStatus
    suspend fun restorePurchases(): Boolean
    suspend fun changePlan(newPlan: PremiumPlan): Boolean
    suspend fun cancelSubscription(): Boolean
}

class PremiumRepositoryImpl(private val context: Context) : PremiumRepository {

    companion object {
        const val FREE_USER_DAILY_LIMIT = 10
        const val PREMIUM_UNLIMITED = -1
    }

    private object PreferencesKeys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val PLAN_ID = stringPreferencesKey("plan_id")
        val SUBSCRIPTION_DATE = longPreferencesKey("subscription_date")
        val EXPIRY_DATE = longPreferencesKey("expiry_date")
        val REMAINING_SCANS = intPreferencesKey("remaining_scans")
        val LAST_RESET_DATE = longPreferencesKey("last_reset_date")
        val DAILY_SCAN_COUNT = intPreferencesKey("daily_scan_count")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    override val premiumStatusFlow: Flow<PremiumStatus> = context.premiumDataStore.data.map { preferences ->
        try {
            val currentTime = System.currentTimeMillis()
            val lastResetDate = preferences[PreferencesKeys.LAST_RESET_DATE] ?: 0L
            val isPremium = preferences[PreferencesKeys.IS_PREMIUM] ?: false
            val expiryDate = preferences[PreferencesKeys.EXPIRY_DATE] ?: 0L

            // Check if premium has expired
            val isActivePremium = isPremium && (expiryDate == 0L || currentTime < expiryDate)

            // Reset daily scans if it's a new day
            val shouldReset = shouldResetDailyScans(lastResetDate, currentTime)

            val remainingScans = if (isActivePremium) {
                PREMIUM_UNLIMITED
            } else {
                val dailyUsed = preferences[PreferencesKeys.DAILY_SCAN_COUNT] ?: 0

                // PERBAIKAN: Reset scan count if it's a new day
                if (shouldReset && dailyUsed > 0) {
                    // For new day reset, give fresh 10 scans
                    FREE_USER_DAILY_LIMIT
                } else if (lastResetDate == 0L) {
                    // PERBAIKAN: Brand new user gets full 10 scans
                    FREE_USER_DAILY_LIMIT
                } else {
                    // Calculate remaining scans for existing users
                    maxOf(0, FREE_USER_DAILY_LIMIT - dailyUsed)
                }
            }

            // PERBAIKAN: Log untuk debugging
            android.util.Log.d("PremiumRepository", "Status - isPremium: $isActivePremium, dailyUsed: ${preferences[PreferencesKeys.DAILY_SCAN_COUNT] ?: 0}, remaining: $remainingScans, shouldReset: $shouldReset")

            PremiumStatus(
                isPremium = isActivePremium,
                planId = preferences[PreferencesKeys.PLAN_ID] ?: "",
                subscriptionDate = preferences[PreferencesKeys.SUBSCRIPTION_DATE] ?: 0L,
                expiryDate = if (expiryDate == 0L) null else expiryDate,
                remainingScans = remainingScans
            )
        } catch (e: Exception) {
            android.util.Log.e("PremiumRepository", "Error in premiumStatusFlow", e)
            // PERBAIKAN: Return safe default dengan full limit untuk user baru
            PremiumStatus(
                isPremium = false,
                planId = "",
                subscriptionDate = 0L,
                expiryDate = null,
                remainingScans = FREE_USER_DAILY_LIMIT // Default: 10/10 untuk user baru
            )
        }
    }

    override fun getAvailablePlans(): List<PremiumPlan> {
        return listOf(
            PremiumPlan(
                id = "premium_monthly",
                name = "Premium Monthly",
                description = "Akses penuh semua fitur premium selama 1 bulan",
                price = "Rp 29.000", // PERBAIKAN: Sesuaikan harga dengan pricePerMonth
                pricePerMonth = 29000.0,
                duration = "1 bulan",
                features = listOf(
                    "Scan unlimited per hari",
                    "Semua format file (JPG, PNG, JPEG, WEBP, BMP)",
                    "Max ukuran file: 50MB",
                    "Analisis mendalam dengan confidence score",
                    "Backup cloud otomatis",
                    "Share hasil Deteksi",
                    "Tanpa iklan",
                    "Priority customer support"
                ),
                isPopular = true, // PERBAIKAN: Monthly menjadi yang paling populer
                discountPercentage = 0
            )

        )
    }

    override suspend fun upgradeToPremium(planId: String) {
        try {
            val currentTime = System.currentTimeMillis()

            context.premiumDataStore.edit { preferences ->
                preferences[PreferencesKeys.IS_PREMIUM] = true
                preferences[PreferencesKeys.PLAN_ID] = planId
                preferences[PreferencesKeys.SUBSCRIPTION_DATE] = currentTime

                // Set expiry date based on plan duration
                val calendar = Calendar.getInstance()
                when (planId) {
                    "premium_monthly" -> {
                        calendar.add(Calendar.MONTH, 1)
                        preferences[PreferencesKeys.EXPIRY_DATE] = calendar.timeInMillis
                    }
                    // PERBAIKAN: Yearly plan dihapus, default ke monthly
                    else -> {
                        // Default to 1 month for unknown plans
                        calendar.add(Calendar.MONTH, 1)
                        preferences[PreferencesKeys.EXPIRY_DATE] = calendar.timeInMillis
                    }
                }

                preferences[PreferencesKeys.USER_ROLE] = UserRole.PREMIUM.name
                // Reset daily scan count saat upgrade ke premium
                preferences[PreferencesKeys.DAILY_SCAN_COUNT] = 0
                preferences[PreferencesKeys.LAST_RESET_DATE] = currentTime
            }

            // PERBAIKAN: Log untuk debugging
            android.util.Log.d("PremiumRepository", "Premium upgrade completed for plan: $planId")

        } catch (e: Exception) {
            android.util.Log.e("PremiumRepository", "Error upgrading to premium", e)
            throw e
        }
    }

    override suspend fun purchasePlan(plan: PremiumPlan): Boolean {
        return try {
            // Simulate payment processing
            // In a real app, this would integrate with payment gateway
            upgradeToPremium(plan.id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getPremiumStatus(): PremiumStatus {
        return premiumStatusFlow.first()
    }

    override suspend fun getScanLimitStatus(): ScanLimitStatus {
        val premiumStatus = getPremiumStatus()

        return if (premiumStatus.isPremium) {
            // PERBAIKAN: Premium user selalu dapat scan tanpa batasan
            ScanLimitStatus(
                canScan = true,
                remainingScans = -1, // Unlimited
                isUnlimited = true,
                message = "Premium: Unlimited scans available"
            )
        } else {
            // PERBAIKAN: Free user dengan logika yang lebih ketat
            val canScan = premiumStatus.remainingScans > 0
            ScanLimitStatus(
                canScan = canScan,
                remainingScans = premiumStatus.remainingScans,
                isUnlimited = false,
                resetTime = getNextResetTime(),
                message = if (canScan) {
                    if (premiumStatus.remainingScans == 10) {
                        "10 scan gratis tersedia hari ini"
                    } else {
                        "${premiumStatus.remainingScans} scan tersisa hari ini"
                    }
                } else {
                    "Batas scan harian tercapai. Upgrade ke Premium untuk unlimited scan!"
                }
            )
        }
    }

    override suspend fun restorePurchases(): Boolean {
        return try {
            // PERBAIKAN: Proper restore logic
            val currentStatus = getPremiumStatus()
            if (currentStatus.isPremium && currentStatus.expiryDate != null) {
                val currentTime = System.currentTimeMillis()
                if (currentTime < currentStatus.expiryDate!!) {
                    // Premium is still valid, just sync
                    syncUserRole(null)
                    true
                } else {
                    // Premium has expired, downgrade
                    cancelSubscription()
                    false
                }
            } else {
                // No premium to restore
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("PremiumRepository", "Error restoring purchases", e)
            false
        }
    }

    private fun shouldResetDailyScans(lastResetDate: Long, currentTime: Long): Boolean {
        val lastResetCalendar = Calendar.getInstance().apply { timeInMillis = lastResetDate }
        val currentCalendar = Calendar.getInstance().apply { timeInMillis = currentTime }

        return lastResetCalendar.get(Calendar.DAY_OF_YEAR) != currentCalendar.get(Calendar.DAY_OF_YEAR) ||
               lastResetCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)
    }

    private suspend fun resetDailyScansInternal() {
        context.premiumDataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_SCAN_COUNT] = 0
            preferences[PreferencesKeys.LAST_RESET_DATE] = System.currentTimeMillis()
        }
    }

    private fun getNextResetTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // ...existing interface methods implementation...
    override suspend fun updateRemainingScans(count: Int) {
        try {
            context.premiumDataStore.edit { preferences ->
                preferences[PreferencesKeys.REMAINING_SCANS] = count
            }
        } catch (e: Exception) {
            // Handle silently or log error
        }
    }

    override suspend fun decrementScanCount(): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()

            // PERBAIKAN: Check and reset daily scans if it's a new day first
            context.premiumDataStore.edit { preferences ->
                val lastResetDate = preferences[PreferencesKeys.LAST_RESET_DATE] ?: 0L
                val shouldReset = shouldResetDailyScans(lastResetDate, currentTime)

                if (shouldReset) {
                    // Reset for new day
                    preferences[PreferencesKeys.DAILY_SCAN_COUNT] = 0
                    preferences[PreferencesKeys.LAST_RESET_DATE] = currentTime
                    android.util.Log.d("PremiumRepository", "Daily scans reset for new day")
                }
            }

            // Now check premium status after potential reset
            val premiumStatus = getPremiumStatus()

            if (premiumStatus.isPremium) {
                return true // Premium users have unlimited scans
            }

            return if (premiumStatus.remainingScans > 0) {
                context.premiumDataStore.edit { preferences ->
                    val currentUsed = preferences[PreferencesKeys.DAILY_SCAN_COUNT] ?: 0
                    preferences[PreferencesKeys.DAILY_SCAN_COUNT] = currentUsed + 1
                    android.util.Log.d("PremiumRepository", "Scan count decremented: ${currentUsed + 1}/${FREE_USER_DAILY_LIMIT}")
                }
                true
            } else {
                android.util.Log.w("PremiumRepository", "Scan limit reached: ${premiumStatus.remainingScans} remaining")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("PremiumRepository", "Error in decrementScanCount", e)
            false
        }
    }

    override suspend fun resetDailyScans() {
        try {
            resetDailyScansInternal()
        } catch (e: Exception) {
            // Handle silently or log error
        }
    }

    override suspend fun canScanToday(): Boolean {
        return try {
            val premiumStatus = getPremiumStatus()
            premiumStatus.isPremium || premiumStatus.remainingScans > 0
        } catch (e: Exception) {
            // Default to false if error occurs
            false
        }
    }

    override suspend fun getRemainingScansCount(): Int {
        return try {
            val premiumStatus = getPremiumStatus()
            premiumStatus.remainingScans
        } catch (e: Exception) {
            // Return 0 if error occurs
            0
        }
    }

    override suspend fun getUserRole(): UserRole {
        return try {
            val premiumStatus = getPremiumStatus()
            if (premiumStatus.isPremium) UserRole.PREMIUM else UserRole.FREE
        } catch (e: Exception) {
            // Default to FREE if error occurs
            UserRole.FREE
        }
    }

    override suspend fun syncUserRole(userPreferencesRepository: UserPreferencesRepository?) {
        try {
            val currentRole = getUserRole()
            // Only sync if userPreferencesRepository is provided
            userPreferencesRepository?.updateUserRole(currentRole)
        } catch (e: Exception) {
            android.util.Log.e("PremiumRepository", "Error syncing user role", e)
            // Handle silently or log error
        }
    }

    override suspend fun changePlan(newPlan: PremiumPlan): Boolean {
        return try {
            // PERBAIKAN: Proper plan change logic
            val currentStatus = getPremiumStatus()
            if (currentStatus.isPremium) {
                // Change to new plan, extend from current date
                upgradeToPremium(newPlan.id)
                true
            } else {
                // First time premium purchase
                purchasePlan(newPlan)
            }
        } catch (e: Exception) {
            android.util.Log.e("PremiumRepository", "Error changing plan", e)
            false
        }
    }

    override suspend fun cancelSubscription(): Boolean {
        return try {
            context.premiumDataStore.edit { preferences ->
                preferences[PreferencesKeys.IS_PREMIUM] = false
                preferences[PreferencesKeys.PLAN_ID] = ""
                preferences[PreferencesKeys.EXPIRY_DATE] = 0L
                preferences[PreferencesKeys.USER_ROLE] = UserRole.FREE.name
                // PERBAIKAN: Reset scan count saat cancel subscription
                preferences[PreferencesKeys.DAILY_SCAN_COUNT] = 0
                preferences[PreferencesKeys.LAST_RESET_DATE] = System.currentTimeMillis()
            }

            // PERBAIKAN: Sync dengan UserPreferencesRepository
            syncUserRole(null)
            true
        } catch (e: Exception) {
            android.util.Log.e("PremiumRepository", "Error canceling subscription", e)
            false
        }
    }
}
