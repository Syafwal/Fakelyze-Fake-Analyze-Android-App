package com.wall.fakelyze.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.wall.fakelyze.domain.model.DetectionResult
import java.io.File
import androidx.compose.ui.tooling.preview.Preview
import java.util.Date

@Preview(showBackground = true)
@Composable
private fun ResultVisualizerPreview() {
    val sampleDetectionResult = DetectionResult(
        id = "preview_id",
        imagePath = "", // Empty for preview
        thumbnailPath = "",
        isAIGenerated = true,
        confidenceScore = 0.95f,
        timestamp = Date()
    )

    MaterialTheme {
        ResultVisualizer(
            detectionResult = sampleDetectionResult
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultVisualizerRealImagePreview() {
    val sampleDetectionResult = DetectionResult(
        id = "preview_id",
        imagePath = "", // Empty for preview
        thumbnailPath = "",
        isAIGenerated = false,
        confidenceScore = 0.85f,
        timestamp = Date()
    )

    MaterialTheme {
        ResultVisualizer(
            detectionResult = sampleDetectionResult
        )
    }
}

@Composable
fun ResultVisualizer(
    detectionResult: DetectionResult,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showProgress by remember { mutableStateOf(false) }
    val confidenceAnimation by animateFloatAsState(
        targetValue = if (showProgress) detectionResult.confidenceScore else 0f,
        label = "confidence"
    )

    LaunchedEffect(key1 = detectionResult) {
        showProgress = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the image
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(File(detectionResult.imagePath))
                    .build()
            ),
            contentDescription = "Analyzed image",
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Detection result heading
        Text(
            text = if (detectionResult.isAIGenerated) "AI GENERATED IMAGE" else "REAL IMAGE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (detectionResult.isAIGenerated)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confidence score visualization
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Confidence: ${(confidenceAnimation * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { confidenceAnimation },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (detectionResult.isAIGenerated)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }

}