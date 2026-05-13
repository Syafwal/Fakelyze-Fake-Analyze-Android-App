package com.wall.fakelyze.ui.component

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wall.fakelyze.BuildConfig
import com.wall.fakelyze.domain.model.DetectionResult
import java.io.File
import java.util.Date

@Preview(showBackground = true)
@Composable
private fun ResultVisualizerPreview() {
    val sampleDetectionResult = DetectionResult(
        id = "preview_id",
        imagePath = "",
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

@Composable
fun ResultVisualizer(
    detectionResult: DetectionResult,
    isPremium: Boolean = false,
    onUpgradeToPremium: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showProgress by remember { mutableStateOf(false) }

    val confidencePercent = (detectionResult.confidenceScore * 100).toInt()

    // ✅ HANYA FAKE vs REAL
    val (resultColor, resultIcon, resultLabel) = if (detectionResult.isAIGenerated) {
        Triple(
            MaterialTheme.colorScheme.error,
            Icons.Default.Warning,
            "AI GENERATED"
        )
    } else {
        Triple(
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle,
            "REAL/ASLI"
        )
    }

    // Confidence animation
    val confidenceAnimation by animateFloatAsState(
        targetValue = if (showProgress) detectionResult.confidenceScore else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1000,
            easing = androidx.compose.animation.core.EaseOutCubic
        ),
        label = "confidence"
    )

    // Trigger animasi
    LaunchedEffect(detectionResult.id, detectionResult.confidenceScore) {
        kotlinx.coroutines.delay(50)
        showProgress = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image Preview
        DisplayImage(imagePath = detectionResult.imagePath, context = context)

        Spacer(modifier = Modifier.height(24.dp))

        // Result Card dengan warna sesuai kategori
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = resultColor.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = resultIcon,
                    contentDescription = null,
                    tint = resultColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = resultLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = resultColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confidence Badge
                Surface(
                    color = resultColor,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "$confidencePercent% Confidence",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confidence Score Visualization
        DisplayConfidenceScore(
            detectionResult = detectionResult,
            confidenceAnimation = confidenceAnimation,
            isPremium = isPremium,
            resultColor = resultColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Explanation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Penjelasan Detail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isPremium) {
                    Text(
                        text = detectionResult.explanation ?: "Tidak ada penjelasan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    PremiumExplanationPrompt(onUpgradeToPremium = onUpgradeToPremium)
                }
            }
        }

        // Premium Feature Hint
        if (!isPremium && onUpgradeToPremium != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = onUpgradeToPremium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ Upgrade ke Premium untuk fitur lengkap",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayImage(
    imagePath: String,
    context: Context
) {
    var isImageLoading by remember { mutableStateOf(true) }
    var hasImageError by remember { mutableStateOf(false) }

    val imageFile = remember(imagePath) {
        if (imagePath.isNotBlank()) File(imagePath) else null
    }

    val isValidFile = remember(imageFile) {
        imageFile != null && imageFile.exists() && imageFile.length() > 0 && imageFile.canRead()
    }

    if (isValidFile && imageFile != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .size(560)
                        .allowHardware(true)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .listener(
                            onStart = { isImageLoading = true; hasImageError = false },
                            onSuccess = { _, _ -> isImageLoading = false; hasImageError = false },
                            onError = { _, _ -> isImageLoading = false; hasImageError = true }
                        )
                        .build()
                ),
                contentDescription = "Gambar yang dianalisis",
                modifier = Modifier.fillMaxSize().padding(4.dp),
                contentScale = ContentScale.Crop
            )

            if (isImageLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }

            if (hasImageError) {
                ImagePlaceholder(message = "Gagal memuat gambar")
            }
        }
    } else {
        ImagePlaceholder(message = "Gambar tidak tersedia")
    }
}

@Composable
private fun ImagePlaceholder(
    message: String = "Gambar tidak tersedia",
    imagePath: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DisplayConfidenceScore(
    detectionResult: DetectionResult,
    confidenceAnimation: Float,
    isPremium: Boolean,
    resultColor: Color
) {
    if (!isPremium) {
        PremiumConfidencePrompt()
        return
    }

    val safeConfidence = detectionResult.confidenceScore.coerceIn(0f, 1f)
    val displayValue = confidenceAnimation.coerceIn(0f, 1f)
    val displayPercentage = (displayValue * 100).toInt()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tingkat Kepercayaan: $displayPercentage%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayValue.coerceAtLeast(0.01f))
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                resultColor.copy(alpha = 0.8f),
                                resultColor
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val confidenceLevel = when {
            safeConfidence >= 0.9f -> "Sangat Tinggi"
            safeConfidence >= 0.8f -> "Tinggi"
            safeConfidence >= 0.7f -> "Sedang"
            safeConfidence >= 0.6f -> "Rendah"
            else -> "Sangat Rendah"
        }

        Text(
            text = "($confidenceLevel)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PremiumConfidencePrompt() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🔒 Tingkat Kepercayaan Premium",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Upgrade ke Premium untuk melihat tingkat kepercayaan detail",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumExplanationPrompt(onUpgradeToPremium: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🔒 Penjelasan AI Tersedia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Upgrade ke Premium untuk melihat penjelasan detail",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            onUpgradeToPremium?.let { callback ->
                Button(
                    onClick = callback,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(" Upgrade Premium")
                }
            }
        }
    }
}
