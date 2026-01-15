package com.wall.fakelyze.data.model

data class PremiumPlan(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val pricePerMonth: Double, // Harga per bulan untuk kalkulasi
    val duration: String, // String deskripsi durasi (misal "1 bulan", "1 tahun")
    val features: List<String>,
    val isPopular: Boolean = false,
    val discountPercentage: Int = 0 // Persentase diskon untuk yearly plan
)

data class PremiumStatus(
    val isPremium: Boolean = false,
    val planId: String? = null,
    val subscriptionDate: Long? = null,
    val expiryDate: Long? = null,
    val remainingScans: Int = 10 // Free tier: 10 scans per day
)

data class ScanLimitStatus(
    val canScan: Boolean,
    val remainingScans: Int,
    val isUnlimited: Boolean,
    val resetTime: Long? = null, // Waktu reset berikutnya untuk free user
    val message: String = ""
)
