package com.wall.fakelyze.ui.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.wall.fakelyze.ui.component.ResultVisualizer
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel,
    onBackClick: () -> Unit,
    isPremium: Boolean = false, // PERBAIKAN: Tambah parameter premium
    onUpgradeToPremium: (() -> Unit)? = null, // PERBAIKAN: Callback untuk upgrade premium
    // PERBAIKAN: Tambahkan parameter untuk menerima data hasil deteksi
    imagePath: String = "",
    thumbnailPath: String = "",
    isAIGenerated: Boolean = false,
    confidenceScore: Float = 0f,
    explanation: String? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    android.util.Log.d("ResultsScreen", "🔄 === RESULTSSCREEN DIMULAI ===")
    android.util.Log.d("ResultsScreen", "📊 Loading: ${uiState.isLoading}")
    android.util.Log.d("ResultsScreen", "📋 HasData: ${uiState.detectionResult != null}")
    android.util.Log.d("ResultsScreen", "❌ Error: ${uiState.errorMessage}")
    android.util.Log.d("ResultsScreen", "📁 Received ImagePath: '$imagePath'")
    android.util.Log.d("ResultsScreen", "📊 Received Confidence: $confidenceScore")
    android.util.Log.d("ResultsScreen", "🤖 Received IsAI: $isAIGenerated")

    // PERBAIKAN: Gunakan data langsung dari parameter yang diterima
    LaunchedEffect(imagePath, isAIGenerated, confidenceScore) {
        if (imagePath.isNotEmpty()) {
            android.util.Log.d("ResultsScreen", "✅ Setting data langsung dari parameter")
            viewModel.setDetectionResult(
                imagePath = imagePath,
                thumbnailPath = thumbnailPath,
                isAIGenerated = isAIGenerated,
                confidenceScore = confidenceScore,
                explanation = explanation
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Deteksi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // PERBAIKAN: Share button dengan logic premium
                    if (uiState.detectionResult != null) {
                        IconButton(
                            onClick = {
                                // PERBAIKAN: Cek premium status sebelum share
                                if (!isPremium) {
                                    // User free - tampilkan upgrade prompt
                                    onUpgradeToPremium?.invoke()
                                    return@IconButton
                                }

                                // User premium - lanjutkan share
                                try {
                                    val imageFile = File(imagePath)
                                    if (imageFile.exists()) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            imageFile
                                        )

                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                            type = "image/*"
                                            putExtra(
                                                android.content.Intent.EXTRA_TEXT,
                                                "Hasil deteksi: ${if (isAIGenerated) "AI Generated" else "Real"} (${(confidenceScore * 100).toInt()}%)"
                                            )
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }

                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan Hasil"))
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ResultsScreen", "Error sharing", e)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = if (isPremium) "Bagikan" else "Bagikan (Premium)",
                                tint = if (isPremium) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Loading state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Memuat hasil deteksi...",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // PERBAIKAN: Simpan errorMessage ke variabel lokal untuk menghindari smart cast error
                        val errorMessage = uiState.errorMessage
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan yang tidak diketahui",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Kembali")
                        }
                    }
                }

                uiState.detectionResult != null -> {
                    // Success state - tampilkan hasil
                    ResultVisualizer(
                        detectionResult = uiState.detectionResult!!,
                        isPremium = isPremium,
                        onUpgradeToPremium = onUpgradeToPremium,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // Fallback state - jika tidak ada data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Tidak ada data hasil deteksi",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Kembali ke Home")
                        }
                    }
                }
            }
        }
    }
}
