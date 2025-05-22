package com.wall.fakelyze.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.wall.fakelyze.domain.model.DetectionResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import java.util.Date

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(File(detectionResult.thumbnailPath))
                        .build()
                ),
                contentDescription = "Image thumbnail",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Result status
                Text(
                    text = if (detectionResult.isAIGenerated) "AI Generated" else "Real Image",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (detectionResult.isAIGenerated)
                        colorScheme.error
                    else
                        colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Confidence score
                Text(
                    text = "Confidence: ${(detectionResult.confidenceScore * 100).toInt()}%",
                    style = typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = dateFormat.format(detectionResult.timestamp),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}