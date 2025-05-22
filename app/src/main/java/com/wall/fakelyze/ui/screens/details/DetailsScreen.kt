package com.wall.fakelyze.ui.screens.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wall.fakelyze.domain.model.DetectionResult
import com.wall.fakelyze.ui.component.ResultVisualizer
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    detectionResultId: String,
    viewModel: DetailViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val detectionResult = viewModel.detectionResult.collectAsStateWithLifecycle().value

    // Load detection result
    LaunchedEffect(key1 = detectionResultId) {
        viewModel.loadDetectionResult(detectionResultId)
    }

    // Handle UI state
    LaunchedEffect(key1 = state) {
        when (state) {
            is DetailUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                if (state.message.contains("not found")) {
                    onBackClick()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detection Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Share action
                    IconButton(
                        onClick = {
                            detectionResult?.let { result ->
                                shareDetectionResult(context, result)
                            }
                        },
                        enabled = detectionResult != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }

                    // Delete action
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val deleted = viewModel.deleteDetectionResult()
                                if (deleted) {
                                    snackbarHostState.showSnackbar("Detection result deleted")
                                    onBackClick()
                                }
                            }
                        },
                        enabled = detectionResult != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (state) {
            DetailUiState.Loading -> {
                // Show loading state
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                )
            }
            is DetailUiState.Success, is DetailUiState.Error -> {
                // Show content if available
                detectionResult?.let { result ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        ResultVisualizer(detectionResult = result)
                    }
                }
            }
        }
    }
}

private fun shareDetectionResult(context: android.content.Context, detectionResult: DetectionResult) {
    val file = File(detectionResult.imagePath)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        putExtra(
            android.content.Intent.EXTRA_TEXT,
            "Image Detection Result: ${if (detectionResult.isAIGenerated) "AI Generated" else "Real"} " +
                    "with ${(detectionResult.confidenceScore * 100).toInt()}% confidence."
        )
        type = "image/jpeg"
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        android.content.Intent.createChooser(
            shareIntent,
            "Share detection result"
        )
    )
}

