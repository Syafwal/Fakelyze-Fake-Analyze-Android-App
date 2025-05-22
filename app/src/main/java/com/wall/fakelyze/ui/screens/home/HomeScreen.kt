package com.wall.fakelyze.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wall.fakelyze.ml.ClassificationResult
import com.wall.fakelyze.ui.component.CameraCapture
import com.wall.fakelyze.ui.component.GalleryPicker

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onNavigateToResults: (String, String, Boolean, Float) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Camera permission
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fakelyze") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tab Row
            val tabs = listOf("Camera", "Gallery")
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.selectedTabIndex) {
                    0 -> {
                        // Camera Tab
                        if (cameraPermissionState.status.isGranted) {
                            CameraCapture(
                                onImageCaptured = { bitmap ->
                                    viewModel.processImage(bitmap) { result ->
                                        when (result) {
                                            is ClassificationResult.Success -> {
                                                onNavigateToResults(
                                                    result.imagePath,
                                                    result.thumbnailPath,
                                                    result.isAIGenerated,
                                                    result.confidenceScore
                                                )
                                            }
                                            is ClassificationResult.Error -> {
                                                // Show error
                                            }
                                        }
                                    }
                                },
                                onError = {
                                    // Handle error
                                }
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Camera Permission Required",
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center
                                )
                                androidx.compose.material3.Button(
                                    onClick = { cameraPermissionState.launchPermissionRequest() },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Request Permission")
                                }
                            }
                        }
                    }
                    1 -> {
                        // Gallery Tab
                        GalleryPicker(
                            onImageSelected = { bitmap ->
                                viewModel.processImage(bitmap) { result ->
                                    when (result) {
                                        is ClassificationResult.Success -> {
                                            onNavigateToResults(
                                                result.imagePath,
                                                result.thumbnailPath,
                                                result.isAIGenerated,
                                                result.confidenceScore
                                            )
                                        }
                                        is ClassificationResult.Error -> {
                                            // Show error
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Show loading indicator when processing
                if (uiState.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(
                                text = "Analyzing image...",
                                modifier = Modifier.padding(top = 16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

