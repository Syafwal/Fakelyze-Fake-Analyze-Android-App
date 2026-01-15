package com.wall.fakelyze.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wall.fakelyze.domain.model.DetectionResult
import java.util.Date
import java.util.UUID

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val imagePath: String,
    val thumbnailPath: String,
    val isAIGenerated: Boolean,
    val confidenceScore: Float,
    val timestamp: Date = Date(),
    val explanation: String? = null  // Menambahkan field explanation
)


fun HistoryEntity.toDetectionResult() = DetectionResult(
    id = id,
    imagePath = imagePath,
    thumbnailPath = thumbnailPath,
    isAIGenerated = isAIGenerated,
    confidenceScore = confidenceScore,
    timestamp = timestamp,
    explanation = explanation  // Meneruskan explanation ke DetectionResult
)