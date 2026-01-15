package com.wall.fakelyze.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wall.fakelyze.domain.model.DetectionResult
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSuccessScreen(
    scanResult: DetectionResult?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var showIcon by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showIcon = true
        delay(300)
        showContent = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon dengan animasi
                AnimatedVisibility(
                    visible = showIcon,
                    enter = scaleIn() + fadeIn()
                ) {
                    Icon(
                        imageVector = if (scanResult?.isAIGenerated == true) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = if (scanResult?.isAIGenerated == true)
                            colorScheme.error
                        else
                            colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Konten dengan animasi
                AnimatedVisibility(
                    visible = showContent,
                    enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Text(
                            text = "Scan Berhasil!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Result summary
                        if (scanResult != null) {
                            Text(
                                text = if (scanResult.isAIGenerated) "AI Generated" else "Real",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (scanResult.isAIGenerated)
                                    colorScheme.error
                                else
                                    colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", scanResult.confidenceScore * 100)}% Yakin",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lihat Detail Hasil")
                        }
                    }
                }
            }
        }
    }
}
