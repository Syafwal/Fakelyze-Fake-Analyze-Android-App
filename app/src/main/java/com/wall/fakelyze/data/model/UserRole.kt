package com.wall.fakelyze.data.model

/**
 * Enum untuk mendefinisikan role user dalam aplikasi
 */
enum class UserRole(val displayName: String, val description: String) {
    FREE("Free", "Akun gratis dengan limit 10 scan per hari"),
    PREMIUM("Premium", "Akun premium dengan scan tak terbatas");

    companion object {
        fun fromString(value: String): UserRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                FREE // Default ke FREE jika role tidak dikenali
            }
        }

        fun fromPremiumStatus(isPremium: Boolean): UserRole {
            return if (isPremium) PREMIUM else FREE
        }
    }
}

/**
 * Data class untuk informasi lengkap user dengan role
 */
data class UserRoleInfo(
    val role: UserRole,
    val scanLimit: Int,
    val isUnlimited: Boolean,
    val privileges: List<String>
) {
    companion object {
        fun getInfoForRole(role: UserRole): UserRoleInfo {
            return when (role) {
                UserRole.FREE -> UserRoleInfo(
                    role = UserRole.FREE,
                    scanLimit = 10,
                    isUnlimited = false,
                    privileges = listOf(
                        "10 scan per hari",
                        "Deteksi gambar AI dasar",
                        "Simpan hasil scan"
                    )
                )
                UserRole.PREMIUM -> UserRoleInfo(
                    role = UserRole.PREMIUM,
                    scanLimit = -1, // Unlimited
                    isUnlimited = true,
                    privileges = listOf(
                        "Scan tak terbatas",
                        "Analisis mendalam",
                        "Laporan detail",
                        "Support prioritas",
                        "Fitur advanced",
                        "Export hasil"
                    )
                )
            }
        }
    }
}
