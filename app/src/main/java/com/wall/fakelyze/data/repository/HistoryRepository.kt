package com.wall.fakelyze.data.repository

import com.wall.fakelyze.data.database.dao.HistoryDao
import com.wall.fakelyze.data.database.entity.HistoryEntity
import com.wall.fakelyze.data.database.entity.toDetectionResult
import com.wall.fakelyze.domain.model.DetectionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HistoryRepository {
    fun getAllHistory(): Flow<List<DetectionResult>>
    suspend fun getHistoryById(id: String): DetectionResult?
    suspend fun saveHistory(detectionResult: DetectionResult)
    suspend fun deleteHistory(detectionResult: DetectionResult)
    suspend fun clearAllHistory()
}

class HistoryRepositoryImpl(private val historyDao: HistoryDao) : HistoryRepository {

    override fun getAllHistory(): Flow<List<DetectionResult>> {
        return historyDao.getAllHistory().map { list ->
            list.map { it.toDetectionResult() }
        }
    }

    override suspend fun getHistoryById(id: String): DetectionResult? {
        return historyDao.getHistoryById(id)?.toDetectionResult()
    }

    override suspend fun saveHistory(detectionResult: DetectionResult) {
        historyDao.insertHistory(
            HistoryEntity(
                id = detectionResult.id,
                imagePath = detectionResult.imagePath,
                thumbnailPath = detectionResult.thumbnailPath,
                isAIGenerated = detectionResult.isAIGenerated,
                confidenceScore = detectionResult.confidenceScore,
                timestamp = detectionResult.timestamp
            )
        )
    }

    override suspend fun deleteHistory(detectionResult: DetectionResult) {
        historyDao.deleteHistory(
            HistoryEntity(
                id = detectionResult.id,
                imagePath = detectionResult.imagePath,
                thumbnailPath = detectionResult.thumbnailPath,
                isAIGenerated = detectionResult.isAIGenerated,
                confidenceScore = detectionResult.confidenceScore,
                timestamp = detectionResult.timestamp
            )
        )
    }

    override suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
    }
}