package com.wall.fakelyze.domain.model

import java.util.Date

data class DetectionResult(
    val id: String,
    val imagePath: String,
    val thumbnailPath: String,
    val isAIGenerated: Boolean,
    val confidenceScore: Float,
    val timestamp: Date = Date(),
    val explanation: String? = null,
    val metadata: Map<String, Any>? = null
)
