package com.wall.fakelyze.ui.component

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Preview(showSystemUi = true)
@Composable
private fun GalleryPickerPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            GalleryPicker(
                onImageSelected = { /* Preview tidak perlu implementasi */ }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 400)
@Composable
private fun GalleryPickerSmallPreview() {
    MaterialTheme {
        GalleryPicker(
            onImageSelected = { /* Preview tidak perlu implementasi */ }
        )
    }
}

@Composable
fun GalleryPicker(
    onImageSelected: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Create a launcher for the gallery with better error handling
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        isLoading = true
        try {
            uri?.let {
                android.util.Log.d("GalleryPicker", "URI gambar dipilih: $it")

                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                        // PERBAIKAN: Set target size untuk mengurangi beban thermal
                        decoder.setTargetSize(1024, 1024)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }

                if (bitmap != null) {
                    // PERBAIKAN: Validasi ukuran bitmap sebelum processing
                    val bitmapSize = bitmap.allocationByteCount
                    android.util.Log.d("GalleryPicker", "Ukuran bitmap: ${bitmapSize / 1024}KB")

                    if (bitmapSize > 20 * 1024 * 1024) { // 20MB limit untuk mengurangi thermal load
                        // Kompres bitmap jika terlalu besar
                        val scaledBitmap = Bitmap.createScaledBitmap(
                            bitmap,
                            bitmap.width / 2,
                            bitmap.height / 2,
                            true
                        )
                        bitmap.recycle() // Release original bitmap
                        hasError = false
                        onImageSelected(scaledBitmap)
                    } else {
                        hasError = false
                        onImageSelected(bitmap)
                    }
                } else {
                    hasError = true
                    errorMessage = "Gagal memuat gambar dari galeri"
                }
            } ?: run {
                android.util.Log.d("GalleryPicker", "Pemilihan gambar dibatalkan")
                hasError = false
            }
        } catch (e: OutOfMemoryError) {
            android.util.Log.e("GalleryPicker", "Out of memory loading image", e)
            hasError = true
            errorMessage = "Gambar terlalu besar. Pilih gambar yang lebih kecil."
        } catch (e: Exception) {
            android.util.Log.e("GalleryPicker", "Error loading image from gallery", e)
            hasError = true
            errorMessage = when {
                e.message?.contains("thermal", ignoreCase = true) == true ->
                    "Device terlalu panas. Tunggu sebentar dan coba lagi."
                e.message?.contains("storage", ignoreCase = true) == true ->
                    "Tidak dapat mengakses penyimpanan. Periksa izin aplikasi."
                else -> "Error: ${e.localizedMessage ?: "Tidak dapat memuat gambar"}"
            }
        } finally {
            isLoading = false
        }
    }

    // PERBAIKAN: Container yang konsisten dengan CameraCapture
    Box(
        modifier = Modifier
            .fillMaxSize() // Menggunakan ruang yang tersedia
            .background(MaterialTheme.colorScheme.surface) // Background konsisten
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasError) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )
            }

            Text(
                text = "Tekan tombol di bawah untuk memilih gambar dari galeri",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        // PERBAIKAN: Tombol dengan posisi yang konsisten dengan CameraCapture
        FloatingActionButton(
            onClick = {
                hasError = false
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp) // Padding konsisten dengan CameraCapture
                .size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoLibrary,
                contentDescription = "Pilih dari galeri",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // PERBAIKAN: Loading indicator yang konsisten
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Memuat gambar...",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
