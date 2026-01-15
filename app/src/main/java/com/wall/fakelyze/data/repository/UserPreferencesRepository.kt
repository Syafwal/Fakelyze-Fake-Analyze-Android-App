package com.wall.fakelyze.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wall.fakelyze.data.datastore.userPreferencesDataStore
import com.wall.fakelyze.data.model.UserRole
import com.wall.fakelyze.data.model.UserRoleInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val isDarkTheme: Boolean = false,
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "FREE", // Default ke FREE
    val userRole: UserRole = UserRole.FREE, // Enum role
    val scanCount: Int = 0,
    val aiDetectedCount: Int = 0,
    val realImageCount: Int = 0,
    val profilePhotoUri: String? = null
)

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateDarkTheme(isDarkTheme: Boolean)
    suspend fun updateUserProfile(username: String, displayName: String, email: String, phone: String, role: String = "")
    suspend fun updateUserRole(role: UserRole)
    suspend fun upgradeToRole(newRole: UserRole)
    suspend fun downgradeToRole(newRole: UserRole)
    suspend fun incrementScanCount()
    suspend fun incrementAiDetectedCount()
    suspend fun incrementRealImageCount()
    suspend fun updateProfilePhoto(uri: String)
    suspend fun clearUserData()
    fun getUserRoleInfo(role: UserRole): UserRoleInfo
    suspend fun syncRoleWithPremiumStatus(isPremium: Boolean)
}

class UserPreferencesRepositoryImpl(private val context: Context) : UserPreferencesRepository {

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val USERNAME = stringPreferencesKey("username")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
        val ROLE = stringPreferencesKey("role")
        val SCAN_COUNT = stringPreferencesKey("scan_count")
        val AI_DETECTED_COUNT = stringPreferencesKey("ai_detected_count")
        val REAL_IMAGE_COUNT = stringPreferencesKey("real_image_count")
        val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { preferences ->
        val isDarkTheme = preferences[PreferencesKeys.DARK_THEME] ?: false
        val username = preferences[PreferencesKeys.USERNAME] ?: ""
        val displayName = preferences[PreferencesKeys.DISPLAY_NAME] ?: ""
        val email = preferences[PreferencesKeys.EMAIL] ?: ""
        val phone = preferences[PreferencesKeys.PHONE] ?: ""
        val roleString = preferences[PreferencesKeys.ROLE] ?: "FREE"
        val userRole = UserRole.fromString(roleString)
        val scanCount = preferences[PreferencesKeys.SCAN_COUNT]?.toIntOrNull() ?: 0
        val aiDetectedCount = preferences[PreferencesKeys.AI_DETECTED_COUNT]?.toIntOrNull() ?: 0
        val realImageCount = preferences[PreferencesKeys.REAL_IMAGE_COUNT]?.toIntOrNull() ?: 0
        val profilePhotoUri = preferences[PreferencesKeys.PROFILE_PHOTO_URI]

        UserPreferences(
            isDarkTheme = isDarkTheme,
            username = username,
            displayName = displayName,
            email = email,
            phone = phone,
            role = roleString,
            userRole = userRole,
            scanCount = scanCount,
            aiDetectedCount = aiDetectedCount,
            realImageCount = realImageCount,
            profilePhotoUri = profilePhotoUri
        )
    }

    override suspend fun updateDarkTheme(isDarkTheme: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME] = isDarkTheme
        }
    }

    override suspend fun updateUserProfile(username: String, displayName: String, email: String, phone: String, role: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.DISPLAY_NAME] = displayName
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.PHONE] = phone
            if (role.isNotEmpty()) {
                preferences[PreferencesKeys.ROLE] = role
            }
        }
    }

    override suspend fun updateUserRole(role: UserRole) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.ROLE] = role.name
        }
    }

    override suspend fun upgradeToRole(newRole: UserRole) {
        context.userPreferencesDataStore.edit { preferences ->
            val currentRoleString = preferences[PreferencesKeys.ROLE] ?: "FREE"
            val currentRole = UserRole.fromString(currentRoleString)

            // Hanya upgrade jika role baru lebih tinggi
            if (newRole == UserRole.PREMIUM && currentRole == UserRole.FREE) {
                preferences[PreferencesKeys.ROLE] = newRole.name
            }
        }
    }

    override suspend fun downgradeToRole(newRole: UserRole) {
        context.userPreferencesDataStore.edit { preferences ->
            val currentRoleString = preferences[PreferencesKeys.ROLE] ?: "FREE"
            val currentRole = UserRole.fromString(currentRoleString)

            // Hanya downgrade jika role baru lebih rendah
            if (newRole == UserRole.FREE && currentRole == UserRole.PREMIUM) {
                preferences[PreferencesKeys.ROLE] = newRole.name
            }
        }
    }

    override suspend fun syncRoleWithPremiumStatus(isPremium: Boolean) {
        val targetRole = UserRole.fromPremiumStatus(isPremium)
        updateUserRole(targetRole)
    }

    override suspend fun incrementScanCount() {
        context.userPreferencesDataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.SCAN_COUNT]?.toIntOrNull() ?: 0
            preferences[PreferencesKeys.SCAN_COUNT] = (currentCount + 1).toString()
        }
    }

    override suspend fun incrementAiDetectedCount() {
        context.userPreferencesDataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.AI_DETECTED_COUNT]?.toIntOrNull() ?: 0
            preferences[PreferencesKeys.AI_DETECTED_COUNT] = (currentCount + 1).toString()
        }
    }

    override suspend fun incrementRealImageCount() {
        context.userPreferencesDataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.REAL_IMAGE_COUNT]?.toIntOrNull() ?: 0
            preferences[PreferencesKeys.REAL_IMAGE_COUNT] = (currentCount + 1).toString()
        }
    }

    override suspend fun updateProfilePhoto(uri: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_PHOTO_URI] = uri
        }
    }

    override suspend fun clearUserData() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    override fun getUserRoleInfo(role: UserRole): UserRoleInfo {
        return UserRoleInfo.getInfoForRole(role)
    }
}
