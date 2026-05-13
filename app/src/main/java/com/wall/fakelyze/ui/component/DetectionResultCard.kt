package com.wall.fakelyze.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
    isPremium: Boolean = false,
    onUpgradeToPremium: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    // ✅ HANYA FAKE vs REAL
    val (backgroundColor, textColor, resultLabel) = if (detectionResult.isAIGenerated) {
        Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            "AI Generated"
        )
    } else {
        Triple(
            Color(0xFFC8E6C9),
            Color(0xFF2E7D32),
            "Real/Asli"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image
            val imageFile = remember(detectionResult.thumbnailPath) {
                File(detectionResult.thumbnailPath)
            }

            if (imageFile.exists()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imageFile)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Information column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Result badge
                Surface(
                    color = backgroundColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = resultLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Confidence score
                if (isPremium) {
                    Text(
                        text = "Confidence: ${(detectionResult.confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Premium Feature",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Timestamp
                Text(
                    text = dateFormat.format(detectionResult.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✅ Icon sesuai FAKE vs REAL
            Icon(
                imageVector = if (detectionResult.isAIGenerated) {
                    Icons.Default.Warning
                } else {
                    Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
