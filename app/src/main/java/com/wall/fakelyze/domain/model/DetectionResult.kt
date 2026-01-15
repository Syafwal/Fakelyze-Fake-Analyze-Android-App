package com.wall.fakelyze.domain.model

import java.util.Date
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class DetectionResult(
    val id: String, // PERBAIKAN: Hapus default UUID random untuk konsistensi
    val imagePath: String,
    val thumbnailPath: String,
    val isAIGenerated: Boolean,
    val confidenceScore: Float,
    val timestamp: Date = Date(),
    val explanation: String? = null, // Penjelasan model (Explainability)
    val metadata: Map<String, String>? = null, // Detail metadata gambar
    val userFeedback: String? = null // Feedback pengguna
)
