package com.wall.fakelyze.ui.screens.results

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.domain.model.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class ResultsViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ResultsViewModel"
    }

    // UI state
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    // PERBAIKAN: Method untuk set detection result langsung dari parameter
    fun setDetectionResult(
        imagePath: String,
        thumbnailPath: String,
        isAIGenerated: Boolean,
        confidenceScore: Float,
        explanation: String? = null
    ) {
        Log.d(TAG, "🔄 === SET DETECTION RESULT ===")
        Log.d(TAG, "📁 ImagePath: '$imagePath'")
        Log.d(TAG, "📁 ThumbnailPath: '$thumbnailPath'")
        Log.d(TAG, "🤖 IsAI: $isAIGenerated")
        Log.d(TAG, "📊 Confidence: $confidenceScore (${(confidenceScore * 100).toInt()}%)")
        Log.d(TAG, "📝 Explanation: ${explanation?.take(50)}...")

        try {
            // PERBAIKAN: Validasi minimal - pastikan data essential ada
            val finalImagePath = if (imagePath.isNotBlank()) imagePath else ""
            val finalThumbnailPath = if (thumbnailPath.isNotBlank()) thumbnailPath else finalImagePath
            val finalConfidence = confidenceScore.coerceIn(0.0f, 1.0f)
            val finalExplanation = explanation ?: generateDefaultExplanation(isAIGenerated, finalConfidence)

            // PERBAIKAN: Buat DetectionResult dengan data yang valid
            val detectionResult = DetectionResult(
                id = "temp_${System.currentTimeMillis()}",
                imagePath = finalImagePath,
                thumbnailPath = finalThumbnailPath,
                isAIGenerated = isAIGenerated,
                confidenceScore = finalConfidence,
                explanation = finalExplanation,
                timestamp = Date() // PERBAIKAN: Gunakan timestamp bukan scanDate
            )

            Log.d(TAG, "✅ DetectionResult created successfully")
            Log.d(TAG, "📋 ID: ${detectionResult.id}")
            Log.d(TAG, "📁 Final ImagePath: '${detectionResult.imagePath}'")
            Log.d(TAG, "📊 Final Confidence: ${detectionResult.confidenceScore}")

            // PERBAIKAN: Update UI state dengan data yang sudah divalidasi
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = null,
                detectionResult = detectionResult
            )

            Log.d(TAG, "✅ UI State updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting detection result", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Gagal memuat hasil deteksi: ${e.localizedMessage}",
                detectionResult = null
            )
        }
    }

    // Load detection result dengan optimasi loading dan validasi data yang ketat
    fun loadDetectionResult(
        imagePath: String,
        thumbnailPath: String,
        isAIGenerated: Boolean,
        confidenceScore: Float,
        explanation: String? = null
    ) {
        Log.d(TAG, "🔄 === MULAI LOAD DETECTION RESULT ===")
        Log.d(TAG, "📁 ImagePath: '$imagePath'")
        Log.d(TAG, "📁 ThumbnailPath: '$thumbnailPath'")
        Log.d(TAG, "🤖 IsAI: $isAIGenerated")
        Log.d(TAG, "📊 Confidence: $confidenceScore (${(confidenceScore * 100).toInt()}%)")
        Log.d(TAG, "📝 Explanation: ${explanation?.take(50)}...")

        // PERBAIKAN: LANGSUNG set data tanpa validasi berlebihan
        _uiState.value = _uiState.value.copy(
            isLoading = false, // LANGSUNG set false, tidak perlu loading animation
            errorMessage = null,
            detectionResult = null
        )

        try {
            // PERBAIKAN: Validasi minimal - hanya pastikan data essential ada
            val finalImagePath = if (imagePath.isNotBlank()) imagePath else ""
            val finalThumbnailPath = if (thumbnailPath.isNotBlank()) thumbnailPath else finalImagePath
            val finalConfidence = confidenceScore.coerceIn(0.0f, 1.0f)
            val finalExplanation = explanation ?: generateDefaultExplanation(isAIGenerated, finalConfidence)

            // PERBAIKAN: Buat DetectionResult dengan data yang valid
            val detectionResult = DetectionResult(
                id = "temp_${System.currentTimeMillis()}",
                imagePath = finalImagePath,
                thumbnailPath = finalThumbnailPath,
                isAIGenerated = isAIGenerated,
                confidenceScore = finalConfidence,
                explanation = finalExplanation,
                timestamp = Date() // PERBAIKAN: Gunakan timestamp bukan scanDate
            )

            Log.d(TAG, "✅ DetectionResult created successfully")
            Log.d(TAG, "📋 ID: ${detectionResult.id}")
            Log.d(TAG, "📁 Final ImagePath: '${detectionResult.imagePath}'")
            Log.d(TAG, "📊 Final Confidence: ${detectionResult.confidenceScore}")

            // PERBAIKAN: Update UI state dengan data yang sudah divalidasi
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = null,
                detectionResult = detectionResult
            )

            Log.d(TAG, "✅ UI State updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading detection result", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Gagal memuat hasil deteksi: ${e.localizedMessage}",
                detectionResult = null
            )
        }
    }

    // ✅ GENERATE EXPLANATION - HAPUS UNCERTAIN
    private fun generateDefaultExplanation(isAIGenerated: Boolean, confidence: Float): String {
        val percentage = (confidence * 100).toInt()

        return when {
            isAIGenerated && confidence >= 0.8f ->
                "Gambar ini kemungkinan besar ($percentage%) dibuat oleh AI. Terdeteksi pola-pola yang konsisten dengan generasi AI."
            isAIGenerated && confidence >= 0.6f ->
                "Gambar ini cukup mungkin ($percentage%) dibuat oleh AI. Ada beberapa indikator yang menunjukkan kemungkinan generasi AI."
            isAIGenerated ->
                "Gambar ini mungkin ($percentage%) dibuat oleh AI, namun tingkat kepercayaan rendah."
            !isAIGenerated && confidence >= 0.8f ->
                "Gambar ini kemungkinan besar ($percentage%) adalah foto asli. Tidak terdeteksi tanda-tanda generasi AI yang signifikan."
            !isAIGenerated && confidence >= 0.6f ->
                "Gambar ini cukup mungkin ($percentage%) adalah foto asli. Karakteristik alami lebih dominan."
            else ->
                "Gambar ini mungkin ($percentage%) adalah foto asli, namun analisis memerlukan verifikasi lebih lanjut."
        }
    }

    // PERBAIKAN: Method untuk loading dari history repository
    fun loadDetectionResultFromHistory(resultId: String) {
        Log.d(TAG, "🔄 Loading detection result from history: $resultId")

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // PERBAIKAN: Gunakan method yang benar dari HistoryRepository
                val result = historyRepository.getHistoryById(resultId)

                // PERBAIKAN: Update UI state dengan proper context switching
                withContext(Dispatchers.Main) {
                    _uiState.value = if (result != null) {
                        Log.d(TAG, "✅ Detection result loaded from history")
                        _uiState.value.copy(
                            isLoading = false,
                            detectionResult = result
                        )
                    } else {
                        Log.e(TAG, "❌ Detection result not found in history")
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Hasil deteksi tidak ditemukan"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading from history", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat hasil deteksi: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // PERBAIKAN: Method untuk clear state
    fun clearState() {
        _uiState.value = ResultsUiState()
    }
}

// PERBAIKAN: Data class untuk UI state
data class ResultsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val detectionResult: DetectionResult? = null
)
