package com.wall.fakelyze.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wall.fakelyze.ui.component.CameraCapture
import com.wall.fakelyze.ui.component.GalleryPicker
import com.wall.fakelyze.ui.component.ScanLimitReachedDialog
import androidx.compose.runtime.*




@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onNavigateToResults: (String, String, Boolean, Float, String?) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showScanLimitDialog by remember { mutableStateOf(false) }
    var limitMessage by remember { mutableStateOf("") }

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val tabs = listOf("Camera", "Galeri")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fakelyze") })
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = {
                            if (index == 0) {
                                if (!cameraPermissionState.status.isGranted) {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                                // Selalu pindah ke tab kamera meskipun permission belum granted
                                viewModel.setSelectedTab(index)
                            } else {
                                viewModel.setSelectedTab(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                color = if (uiState.selectedTabIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.selectedTabIndex) {
                    0 -> {
                        // Camera Tab
                        if (cameraPermissionState.status.isGranted) {
                            CameraCapture(
                                onImageCaptured = { bitmap ->
                                    val time = System.currentTimeMillis()
                                    val imagePath = "camera_$time.jpg"
                                    val thumbnailPath = "thumb_$time.jpg"

                                    viewModel.processImage(
                                        bitmap = bitmap,
                                        imagePath = imagePath,
                                        thumbnailPath = thumbnailPath,
                                        onResult = { tempId, savedImagePath, isAI, confidence, explanation ->
                                            android.util.Log.d("HomeScreen", "🔥 KAMERA: Hasil deteksi siap, mulai navigasi")
                                            android.util.Log.d("HomeScreen", "📊 Data: AI=$isAI, Confidence=${(confidence * 100).toInt()}%")
                                            android.util.Log.d("HomeScreen", "📋 TempId: $tempId")
                                            android.util.Log.d("HomeScreen", "📁 SavedImagePath: $savedImagePath")

                                            // PERBAIKAN: Validasi data sebelum navigasi tanpa return
                                            if (tempId.isNotBlank() && savedImagePath.isNotBlank()) {
                                                // PERBAIKAN: Validasi file exists sebelum navigasi
                                                val imageFile = java.io.File(savedImagePath)
                                                if (imageFile.exists() && imageFile.length() > 0) {
                                                    // PERBAIKAN: Panggil callback langsung dengan path yang benar
                                                    try {
                                                        onNavigateToResults(tempId, savedImagePath, isAI, confidence, explanation)
                                                        android.util.Log.d("HomeScreen", "✅ Callback berhasil dipanggil untuk kamera")
                                                        android.util.Log.d("HomeScreen", "📁 File validated: ${imageFile.absolutePath}")
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("HomeScreen", "❌ Error callback kamera: ${e.message}")
                                                    }
                                                } else {
                                                    android.util.Log.e("HomeScreen", "❌ File gambar tidak valid: $savedImagePath")
                                                    android.util.Log.e("HomeScreen", "❌ File exists: ${imageFile.exists()}, size: ${imageFile.length()}")
                                                }
                                            } else {
                                                android.util.Log.e("HomeScreen", "❌ TempId atau ImagePath kosong")
                                                android.util.Log.e("HomeScreen", "❌ TempId: '$tempId', ImagePath: '$savedImagePath'")
                                            }
                                        },
                                        onError = { error ->
                                            android.util.Log.e("HomeScreen", "❌ Error proses kamera: $error")
                                            if (error.contains("Batas scan harian", ignoreCase = true)) {
                                                limitMessage = error
                                                showScanLimitDialog = true
                                            }
                                        }
                                    )
                                },
                                onError = { error ->
                                    android.util.Log.e("HomeScreen", "❌ Error kamera: ${error.message}")
                                }
                            )
                        } else {
                            CameraPermissionRequest(
                                onRequestPermission = {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            )
                        }
                    }

                    1 -> {
                        // Gallery Tab
                        GalleryPicker(
                            onImageSelected = { bitmap ->
                                val time = System.currentTimeMillis()
                                val imagePath = "gallery_$time.jpg"
                                val thumbnailPath = "thumb_$time.jpg"

                                viewModel.processImage(
                                    bitmap = bitmap,
                                    imagePath = imagePath,
                                    thumbnailPath = thumbnailPath,
                                    onResult = { tempId, savedImagePath, isAI, confidence, explanation ->
                                        android.util.Log.d("HomeScreen", "🔥 GALERI: Hasil deteksi siap, mulai navigasi")
                                        android.util.Log.d("HomeScreen", "📊 Data: AI=$isAI, Confidence=${(confidence * 100).toInt()}%")
                                        android.util.Log.d("HomeScreen", "📋 TempId: $tempId")
                                        android.util.Log.d("HomeScreen", "📁 SavedImagePath: $savedImagePath")

                                        // PERBAIKAN: Validasi data sebelum navigasi tanpa return
                                        if (tempId.isNotBlank() && savedImagePath.isNotBlank()) {
                                            // PERBAIKAN: Validasi file exists sebelum navigasi
                                            val imageFile = java.io.File(savedImagePath)
                                            if (imageFile.exists() && imageFile.length() > 0) {
                                                // PERBAIKAN: Panggil callback langsung dengan path yang benar
                                                try {
                                                    onNavigateToResults(tempId, savedImagePath, isAI, confidence, explanation)
                                                    android.util.Log.d("HomeScreen", "✅ Callback berhasil dipanggil untuk galeri")
                                                    android.util.Log.d("HomeScreen", "���� File validated: ${imageFile.absolutePath}")
                                                } catch (e: Exception) {
                                                    android.util.Log.e("HomeScreen", "❌ Error callback galeri: ${e.message}")
                                                }
                                            } else {
                                                android.util.Log.e("HomeScreen", "❌ File gambar tidak valid: $savedImagePath")
                                                android.util.Log.e("HomeScreen", "❌ File exists: ${imageFile.exists()}, size: ${imageFile.length()}")
                                            }
                                        } else {
                                            android.util.Log.e("HomeScreen", "❌ TempId atau ImagePath kosong")
                                            android.util.Log.e("HomeScreen", "❌ TempId: '$tempId', ImagePath: '$savedImagePath'")
                                        }
                                    },
                                    onError = { error ->
                                        android.util.Log.e("HomeScreen", "❌ Error proses galeri: $error")
                                        if (error.contains("Batas scan harian", ignoreCase = true)) {
                                            limitMessage = error
                                            showScanLimitDialog = true
                                        }
                                    }
                                )
                            }
                        )
                    }
                }

                // Loading indicator
                if (uiState.isProcessing) {
                    ProcessingOverlay()
                }
            }
        }

        // Dialog for scan limit reached
        if (showScanLimitDialog) {
            ScanLimitReachedDialog(
                message = limitMessage,
                onDismiss = { showScanLimitDialog = false },
                onUpgradeToPremium = {
                    showScanLimitDialog = false
                    onNavigateToPremium()
                }
            )
        }
    }
}

@Composable
fun CameraPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Izin Kamera Diperlukan",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Aplikasi memerlukan izin kamera untuk mengambil foto dan melakukan deteksi.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Berikan Izin Kamera")
        }
    }
}

@Composable
fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)), // PERBAIKAN: Hapus background hitam
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Menganalisis gambar...",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface // PERBAIKAN: Gunakan warna tema
            )
        }
    }
}
