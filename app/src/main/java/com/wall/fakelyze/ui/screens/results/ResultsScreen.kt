package com.wall.fakelyze.ui.screens.results

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.wall.fakelyze.ui.component.ResultVisualizer
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    imagePath: String,
    thumbnailPath: String,
    isAIGenerated: Boolean,
    confidenceScore: Float,
    onBackClick: () -> Unit,
    viewModel: ResultsViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    // Load detection result
    LaunchedEffect(key1 = imagePath) {
        viewModel.loadDetectionResult(
            imagePath = imagePath,
            thumbnailPath = thumbnailPath,
            isAIGenerated = isAIGenerated,
            confidenceScore = confidenceScore
        )
    }

    // Handle error messages
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detection Result") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            uiState.detectionResult?.let { detectionResult ->
                                shareDetectionResult(context, detectionResult)
                            }
                        },
                        enabled = uiState.detectionResult != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            uiState.detectionResult?.let { detectionResult ->
                ResultVisualizer(detectionResult = detectionResult)
            }
        }
    }
}

private fun shareDetectionResult(context: android.content.Context, detectionResult: com.wall.fakelyze.domain.model.DetectionResult) {
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

