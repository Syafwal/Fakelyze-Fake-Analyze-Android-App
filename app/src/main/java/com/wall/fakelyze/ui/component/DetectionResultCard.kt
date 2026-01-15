package com.wall.fakelyze.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wall.fakelyze.domain.model.DetectionResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun DetectionResultCardPreviewAI() {
    val sampleDetectionResult = DetectionResult(
        id = "preview_id_1",
        imagePath = "",
        thumbnailPath = "",
        isAIGenerated = true,
        confidenceScore = 0.95f,
        timestamp = Date()
    )

    MaterialTheme {
        DetectionResultCard(
            detectionResult = sampleDetectionResult,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetectionResultCardPreviewReal() {
    val sampleDetectionResult = DetectionResult(
        id = "preview_id_2",
        imagePath = "",
        thumbnailPath = "",
        isAIGenerated = false,
        confidenceScore = 0.87f,
        timestamp = Date()
    )

    MaterialTheme {
        DetectionResultCard(
            detectionResult = sampleDetectionResult,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetectionResultCardPreviewColumn() {
    val aiResult = DetectionResult(
        id = "preview_id_3",
        imagePath = "",
        thumbnailPath = "",
        isAIGenerated = true,
        confidenceScore = 0.95f,
        timestamp = Date()
    )

    val realResult = DetectionResult(
        id = "preview_id_4",
        imagePath = "",
        thumbnailPath = "",
        isAIGenerated = false,
        confidenceScore = 0.87f,
        timestamp = Date()
    )

    MaterialTheme {
        Column {
            DetectionResultCard(
                detectionResult = aiResult,
                onClick = {}
            )
            DetectionResultCard(
                detectionResult = realResult,
                onClick = {}
            )
        }
    }
}

@Composable
fun DetectionResultCard(
    detectionResult: DetectionResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFeedbackSubmit: ((String) -> Unit)? = null,
    isPremium: Boolean = false,
    onUpgradeToPremium: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val feedbackState = remember { mutableStateOf(detectionResult.userFeedback ?: "") }
    val feedbackInputState = remember { mutableStateOf("") }
    val showFeedbackInput = remember { mutableStateOf(false) }

    // Menggunakan warna netral yang sama untuk semua kartu
    val borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    // Animated confidence progress
    val animatedConfidence = animateFloatAsState(
        targetValue = detectionResult.confidenceScore,
        animationSpec = tween(durationMillis = 900), label = "confidenceAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp), // PERBAIKAN: Padding yang lebih kecil
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // PERBAIKAN: Elevation yang lebih kecil
        border = borderStroke
    ) {
        Column(modifier = Modifier.padding(12.dp)) { // PERBAIKAN: Padding internal yang lebih kecil
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PERBAIKAN: Thumbnail dengan ukuran container yang konsisten
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 80.dp) // PERBAIKAN: Container persegi yang konsisten
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(File(detectionResult.thumbnailPath))
                                .size(240) // PERBAIKAN: Ukuran optimal untuk kualitas
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED) // PERBAIKAN: Enable memory cache untuk stabilitas
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .allowHardware(false) // PERBAIKAN: Disable hardware untuk orientasi yang benar
                                .allowRgb565(false) // PERBAIKAN: Disable RGB565 untuk kualitas yang lebih stabil
                                .crossfade(false) // PERBAIKAN: Disable crossfade untuk menghindari glitch
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .build()
                        ),
                        contentDescription = "Thumbnail gambar",
                        modifier = Modifier
                            .fillMaxSize() // PERBAIKAN: Mengisi penuh container Box
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop // PERBAIKAN: Crop untuk mengisi penuh tanpa distorsi
                    )
                }

                Spacer(modifier = Modifier.width(12.dp)) // PERBAIKAN: Spacing yang lebih kecil

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon Status dengan ukuran yang lebih kecil
                        Icon(
                            imageVector = if (detectionResult.isAIGenerated) Icons.Default.Android else Icons.Default.CameraAlt,
                            contentDescription = if (detectionResult.isAIGenerated) "AI Icon" else "Camera Icon",
                            tint = if (detectionResult.isAIGenerated) Color(0xFFD32F2F) else Color(0xFF388E3C),
                            modifier = Modifier.size(18.dp) // PERBAIKAN: Ukuran icon yang lebih kecil dari 22dp ke 18dp
                        )
                        Spacer(modifier = Modifier.width(4.dp)) // PERBAIKAN: Spacing yang lebih kecil
                        // Result status
                        Text(
                            text = if (detectionResult.isAIGenerated) "AI Generated" else "Real Image",
                            style = typography.titleSmall, // PERBAIKAN: Typography yang lebih kecil
                            fontWeight = FontWeight.Bold,
                            color = if (detectionResult.isAIGenerated) colorScheme.error else colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp)) // PERBAIKAN: Spacing yang lebih kecil

                    // PERBAIKAN: Confidence score hanya untuk user premium
                    if (isPremium) {
                        // Animated Confidence Progress Bar dengan ukuran yang lebih kecil
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = animatedConfidence.value,
                            color = if (detectionResult.isAIGenerated) Color(0xFFD32F2F) else Color(0xFF388E3C),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp) // PERBAIKAN: Tinggi progress bar yang lebih kecil dari 6dp ke 4dp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Confidence: ${(detectionResult.confidenceScore * 100).toInt()}%",
                            style = typography.bodySmall // PERBAIKAN: Typography yang lebih kecil
                        )
                    } else {
                        // Premium prompt untuk confidence score
                        Text(
                            text = "🔒 Confidence Score Premium",
                            style = typography.bodySmall,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Timestamp
                    Text(
                        text = dateFormat.format(detectionResult.timestamp),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Penjelasan Model (Explainability) - Hanya untuk user premium
            if (!detectionResult.explanation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (isPremium) {
                    Text(
                        text = "Penjelasan: ${detectionResult.explanation}",
                        style = typography.bodySmall
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(0.5.dp, colorScheme.outline) // PERBAIKAN: Border yang lebih tipis
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp), // PERBAIKAN: Padding yang lebih kecil
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔒 Upgrade ke Premium untuk melihat penjelasan AI",
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Detail Metadata
            if (!detectionResult.metadata.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Metadata:",
                    style = typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                detectionResult.metadata.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        style = typography.bodySmall
                    )
                }
            }

            // Feedback Pengguna
            Spacer(modifier = Modifier.height(8.dp))
            if (feedbackState.value.isNotBlank()) {
                Text(
                    text = "Your Feedback: ${feedbackState.value}",
                    style = typography.bodySmall,
                    color = colorScheme.secondary
                )
            } else if (showFeedbackInput.value) {
                OutlinedTextField(
                    value = feedbackInputState.value,
                    onValueChange = { feedbackInputState.value = it },
                    label = { Text("Your feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = {
                    feedbackState.value = feedbackInputState.value
                    showFeedbackInput.value = false
                    onFeedbackSubmit?.invoke(feedbackInputState.value)
                }) {
                    Text("Submit")
                }
            } else {
                Button(onClick = { showFeedbackInput.value = true }) {
                    Text("Feedback")
                }
            }
        }
    }
}
