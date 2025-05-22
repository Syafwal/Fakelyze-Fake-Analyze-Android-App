package com.wall.fakelyze.domain.model

import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class DetectionResult @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val imagePath: String,
    val thumbnailPath: String,
    val isAIGenerated: Boolean,
    val confidenceScore: Float,
    val timestamp: Date = Date()
)