package com.wall.fakelyze.preview

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wall.fakelyze.data.repository.HistoryRepository
import com.wall.fakelyze.data.repository.UserPreferences
import com.wall.fakelyze.data.repository.UserPreferencesRepository
import com.wall.fakelyze.domain.model.DetectionResult
import com.wall.fakelyze.ml.ClassificationResult
import com.wall.fakelyze.ml.ImageClassifier
import com.wall.fakelyze.ui.screens.history.HistoryScreen
import com.wall.fakelyze.ui.screens.home.HomeScreen
import com.wall.fakelyze.ui.screens.learn.LearnScreen
import com.wall.fakelyze.ui.screens.results.ResultsScreen
import com.wall.fakelyze.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.flow.flow


@Preview(showSystemUi = true)
@Composable
fun AppPreview() {
    AppScreens()
}

@Composable
private fun AppScreens() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        PreviewSection("Home Screen") {
            HomeScreen(
                onNavigateToResults = { _, _, _, _ -> },
                imageClassifier = ImageClassifier(
                    context = android.app.Application()
                )
            )
        }

        PreviewSection("History Screen") {
            HistoryScreen(
                historyRepository = FakeHistoryRepository(),
                onItemClick = {}
            )
        }

        PreviewSection("Results Screen") {
            ResultsScreen(
                imagePath = "fake/path/image.jpg",
                thumbnailPath = "fake/path/thumb.jpg",
                isAIGenerated = true,
                confidenceScore = 0.95f,
                onBackClick = {},
                historyRepository = FakeHistoryRepository()
            )
        }

        PreviewSection("Settings Screen") {
            SettingsScreen(
                userPreferencesRepository = FakeUserPreferencesRepository()
            )
        }

        PreviewSection("Learn Screen") {
            LearnScreen()
        }
    }
}

@Composable
private fun PreviewSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        content()
        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 2.dp
        )
    }
}

// Fake repositories untuk preview
private class FakeHistoryRepository : HistoryRepository {
    override fun getAllHistory() = flow { emit(emptyList<DetectionResult>()) }
    override suspend fun getHistoryById(id: String) = null
    override suspend fun saveHistory(detectionResult: DetectionResult) {}
    override suspend fun deleteHistory(detectionResult: DetectionResult) {}
    override suspend fun clearAllHistory() {}
}

private class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val userPreferencesFlow = flow { emit(UserPreferences()) }
    override suspend fun updateDarkTheme(isDarkTheme: Boolean) {}
}

private class FakeImageClassifier(context: android.content.Context = android.app.Application()) {
    private val classifier = ImageClassifier(context)

    suspend fun classifyImage(bitmap: Bitmap) = ClassificationResult.Success(
        imagePath = "fake/path/image.jpg",
        thumbnailPath = "fake/path/thumb.jpg",
        isAIGenerated = true,
        confidenceScore = 0.95f
    )
}