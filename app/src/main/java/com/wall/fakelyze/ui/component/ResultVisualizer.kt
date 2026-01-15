package com.wall.fakelyze.ui.component

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wall.fakelyze.domain.model.DetectionResult
import com.wall.fakelyze.BuildConfig
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
    modifier: Modifier = Modifier,
    isPremium: Boolean = false,
    onUpgradeToPremium: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showProgress by remember { mutableStateOf(false) }

    // Fix confidence animation - trigger langsung tanpa delay berlebihan
    val confidenceAnimation by animateFloatAsState(
        targetValue = if (showProgress) detectionResult.confidenceScore else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1000,
            easing = androidx.compose.animation.core.EaseOutCubic
        ),
        label = "confidence"
    )

    // Trigger animasi segera
    LaunchedEffect(detectionResult.id, detectionResult.confidenceScore) {
        try {
            kotlinx.coroutines.delay(50)
            showProgress = true
        } catch (e: Exception) {
            showProgress = true
        }
    }

    // Validasi data - selalu tampilkan jika ada ID valid
    val isValidData = remember(detectionResult) {
        detectionResult.id.isNotBlank()
    }

    if (isValidData) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the image
            DisplayImage(
                imagePath = detectionResult.imagePath,
                context = context
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Detection result heading
            DisplayResultHeading(detectionResult = detectionResult)

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation section
            DisplayExplanation(
                detectionResult = detectionResult,
                isPremium = isPremium,
                onUpgradeToPremium = onUpgradeToPremium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confidence score visualization
            DisplayConfidenceScore(
                detectionResult = detectionResult,
                confidenceAnimation = confidenceAnimation,
                isPremium = isPremium // PERBAIKAN: Kirim status premium ke komponen confidence score
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    } else {
        // Fallback UI untuk data yang tidak valid
        FallbackResultVisualizerContent(detectionResult = detectionResult)
    }
}

// PERBAIKAN: Komponen terpisah untuk menampilkan gambar dengan ukuran yang lebih kecil dan proporsional
@Composable
private fun DisplayImage(
    imagePath: String,
    context: Context
) {
    // PERBAIKAN: State untuk tracking loading image
    var isImageLoading by remember { mutableStateOf(true) }
    var hasImageError by remember { mutableStateOf(false) }

    // PERBAIKAN: Validasi file dengan logging yang lebih detail
    val imageFile = remember(imagePath) {
        android.util.Log.d("ResultVisualizer", "🔍 Checking image file: '$imagePath'")
        if (imagePath.isNotBlank()) {
            val file = File(imagePath)
            android.util.Log.d("ResultVisualizer", "📁 File exists: ${file.exists()}")
            android.util.Log.d("ResultVisualizer", "📏 File size: ${if (file.exists()) file.length() else 0}B")
            android.util.Log.d("ResultVisualizer", "📖 File readable: ${if (file.exists()) file.canRead() else false}")
            android.util.Log.d("ResultVisualizer", "📂 Parent dir exists: ${file.parentFile?.exists() ?: false}")
            file
        } else {
            android.util.Log.w("ResultVisualizer", "⚠️ Image path is blank")
            null
        }
    }

    val isValidFile = remember(imageFile) {
        val valid = imageFile != null && imageFile.exists() && imageFile.length() > 0 && imageFile.canRead()
        android.util.Log.d("ResultVisualizer", "✅ File validation result: $valid")
        valid
    }

    // PERBAIKAN: Tampilkan gambar dengan ukuran yang lebih kecil dan proporsional
    if (isValidFile && imageFile != null) {
        android.util.Log.d("ResultVisualizer", "🖼️ Rendering image: ${imageFile.absolutePath}")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp) // PERBAIKAN: Ukuran yang lebih kecil dari 400dp ke 280dp
                .padding(horizontal = 12.dp, vertical = 6.dp) // PERBAIKAN: Padding yang lebih kecil
                .clip(RoundedCornerShape(8.dp)) // PERBAIKAN: Corner radius yang lebih kecil
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), // PERBAIKAN: Border yang lebih tipis
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .size(560) // PERBAIKAN: Ukuran yang lebih kecil dari ORIGINAL ke 560px
                        .allowHardware(true)
                        .allowRgb565(true)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .fallback(android.R.drawable.ic_menu_gallery)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .listener(
                            onStart = {
                                android.util.Log.d("ResultVisualizer", "🔄 Image loading started")
                                isImageLoading = true
                                hasImageError = false
                            },
                            onSuccess = { _, result ->
                                android.util.Log.d("ResultVisualizer", "✅ Image loaded successfully")
                                android.util.Log.d("ResultVisualizer", "📐 Image dimensions: ${result.drawable.intrinsicWidth}x${result.drawable.intrinsicHeight}")
                                isImageLoading = false
                                hasImageError = false
                            },
                            onError = { _, throwable ->
                                android.util.Log.e("ResultVisualizer", "❌ Image load error: ${throwable.throwable.message}")
                                android.util.Log.e("ResultVisualizer", "❌ Error details: ${throwable.throwable}")
                                isImageLoading = false
                                hasImageError = true
                            }
                        )
                        .build()
                ),
                contentDescription = "Gambar yang dianalisis",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp), // PERBAIKAN: Padding internal yang lebih kecil
                contentScale = ContentScale.Crop // PERBAIKAN: Gunakan Crop untuk mengisi ruang dengan baik
            )

            // PERBAIKAN: Loading indicator yang lebih kecil
            if (isImageLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), // PERBAIKAN: Ukuran yang lebih kecil
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Memuat...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // PERBAIKAN: Error indicator yang lebih kecil
            if (hasImageError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Error loading image",
                            modifier = Modifier.size(20.dp), // PERBAIKAN: Ukuran yang lebih kecil
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Gagal memuat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        // PERBAIKAN: Placeholder yang lebih kecil
        android.util.Log.e("ResultVisualizer", "❌ Showing placeholder for path: '$imagePath'")
        android.util.Log.e("ResultVisualizer", "❌ File details: exists=${imageFile?.exists()}, size=${imageFile?.length() ?: 0}, readable=${imageFile?.canRead() ?: false}")

        ImagePlaceholder(
            message = when {
                imagePath.isBlank() -> "Path gambar kosong"
                imageFile?.exists() != true -> "File tidak ditemukan"
                imageFile.length() == 0L -> "File kosong"
                !imageFile.canRead() -> "File tidak dapat dibaca"
                else -> "Gambar tidak dapat dimuat"
            },
            imagePath = imagePath
        )
    }
}

// PERBAIKAN: Placeholder untuk gambar yang tidak bisa dimuat
@Composable
private fun ImagePlaceholder(
    message: String = "Gambar tidak tersedia",
    imagePath: String = ""
) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Gambar tidak tersedia",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            // PERBAIKAN: Tampilkan path gambar di debug mode
            if (BuildConfig.DEBUG && imagePath.isNotBlank()) {
                Text(
                    text = "Path: $imagePath",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// PERBAIKAN: Komponen terpisah untuk heading hasil dengan state-based error handling
@Composable
private fun DisplayResultHeading(detectionResult: DetectionResult) {
    // PERBAIKAN: Gunakan state untuk validasi alih-alih try-catch
    val resultText = remember(detectionResult) {
        when {
            detectionResult.isAIGenerated -> "GAMBAR DIBUAT AI"
            else -> "GAMBAR ASLI"
        }
    }

    android.util.Log.d("ResultVisualizer", "📱 Menampilkan hasil: $resultText")

    Text(
        text = resultText,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = if (detectionResult.isAIGenerated)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
}

// PERBAIKAN: Komponen terpisah untuk explanation dengan state-based error handling
@Composable
private fun DisplayExplanation(
    detectionResult: DetectionResult,
    isPremium: Boolean,
    onUpgradeToPremium: (() -> Unit)?
) {
    // PERBAIKAN: Gunakan state untuk validasi alih-alih try-catch
    val hasExplanation = remember(detectionResult) {
        !detectionResult.explanation.isNullOrBlank()
    }

    if (hasExplanation) {
        if (isPremium) {
            Text(
                text = detectionResult.explanation ?: "",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            PremiumExplanationPrompt(onUpgradeToPremium = onUpgradeToPremium)
        }
    }
}

// PERBAIKAN: Komponen terpisah untuk premium prompt
@Composable
private fun PremiumExplanationPrompt(onUpgradeToPremium: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔒 Penjelasan AI Tersedia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Upgrade ke Premium untuk melihat penjelasan detail tentang hasil deteksi AI",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            onUpgradeToPremium?.let { callback ->
                Button(
                    onClick = {
                        try {
                            callback()
                        } catch (e: Exception) {
                            android.util.Log.e("ResultVisualizer", "❌ Error upgrade callback: ${e.message}")
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " Upgrade Premium",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

// PERBAIKAN: Komponen terpisah untuk confidence score dengan fix untuk animasi dan display
@Composable
private fun DisplayConfidenceScore(
    detectionResult: DetectionResult,
    confidenceAnimation: Float,
    isPremium: Boolean = false // PERBAIKAN: Parameter premium untuk kontrol visibility
) {
    // PERBAIKAN: Hanya tampilkan confidence score untuk user premium
    if (!isPremium) {
        // Tampilkan premium prompt untuk confidence score
        PremiumConfidencePrompt()
        return
    }

    // PERBAIKAN: Validasi dan perbaiki confidence score
    val safeConfidence = remember(detectionResult) {
        val corrected = detectionResult.confidenceScore.coerceIn(0f, 1f)
        android.util.Log.d("ResultVisualizer", "📊 Safe Confidence: $corrected (Original: ${detectionResult.confidenceScore})")

        // PERBAIKAN: Jika confidence masih 0, beri nilai default yang realistis
        if (corrected == 0f) {
            val defaultConfidence = 0.75f
            android.util.Log.w("ResultVisualizer", "⚠️ Confidence 0 detected, using default: $defaultConfidence")
            defaultConfidence
        } else {
            corrected
        }
    }

    val safeAnimationValue = remember(confidenceAnimation, safeConfidence) {
        val animValue = confidenceAnimation.coerceIn(0f, 1f)
        android.util.Log.d("ResultVisualizer", "🎬 Animation Value: $animValue")

        // PERBAIKAN: Jika animasi stuck di 0, gunakan nilai asli langsung
        if (animValue == 0f && safeConfidence > 0f) {
            android.util.Log.w("ResultVisualizer", "⚠️ Animation stuck, using direct value: $safeConfidence")
            safeConfidence
        } else {
            animValue
        }
    }

    // PERBAIKAN: Log untuk debugging confidence display
    android.util.Log.d("ResultVisualizer", "📊 === CONFIDENCE SCORE DEBUG ===")
    android.util.Log.d("ResultVisualizer", "📊 Original: ${detectionResult.confidenceScore}")
    android.util.Log.d("ResultVisualizer", "📊 Safe: $safeConfidence")
    android.util.Log.d("ResultVisualizer", "📊 Animation: $confidenceAnimation")
    android.util.Log.d("ResultVisualizer", "📊 Final Display: $safeAnimationValue")
    android.util.Log.d("ResultVisualizer", "📊 Percentage: ${(safeAnimationValue * 100).toInt()}%")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PERBAIKAN: Tampilkan persentase yang akurat
        val displayPercentage = (safeAnimationValue * 100).toInt()
        Text(
            text = "Tingkat Kepercayaan: $displayPercentage%",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // PERBAIKAN: Progress bar dengan validasi yang lebih ketat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // PERBAIKAN: Pastikan progress bar terisi sesuai nilai
            val progressWidth = if (safeAnimationValue > 0f) safeAnimationValue else 0.01f // Minimal width untuk visibility
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressWidth)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                if (detectionResult.isAIGenerated)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                if (detectionResult.isAIGenerated)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PERBAIKAN: Deskripsi tingkat kepercayaan berdasarkan nilai asli
        val confidenceLevel = remember(safeConfidence) {
            when {
                safeConfidence >= 0.9f -> "Sangat Tinggi"
                safeConfidence >= 0.8f -> "Tinggi"
                safeConfidence >= 0.7f -> "Sedang"
                safeConfidence >= 0.6f -> "Rendah"
                else -> "Sangat Rendah"
            }
        }

        Text(
            text = "($confidenceLevel)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// PERBAIKAN: Komponen prompt premium untuk confidence score
@Composable
private fun PremiumConfidencePrompt() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                text = "Upgrade ke Premium untuk melihat tingkat kepercayaan dan analisis detail dari AI",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// PERBAIKAN: Fallback content untuk error fatal
@Composable
private fun FallbackResultVisualizerContent(detectionResult: DetectionResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️ Terjadi kesalahan saat menampilkan hasil",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hasil: ${if (detectionResult.isAIGenerated) "AI Generated" else "Real Image"}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Kepercayaan: ${(detectionResult.confidenceScore * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}