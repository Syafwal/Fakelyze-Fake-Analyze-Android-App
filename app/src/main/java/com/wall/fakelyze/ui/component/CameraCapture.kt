package com.wall.fakelyze.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

@Preview(showSystemUi = true)
@Composable
private fun CameraCapturePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Mock camera preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            )

            // Camera capture button
            FloatingActionButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Camera,
                    contentDescription = "Ambil foto"
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraCapture(
    onImageCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var retryCount by remember { mutableStateOf(0) }
    var isInitializing by remember { mutableStateOf(true) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    // PERBAIKAN: Preview dengan konfigurasi yang lebih cepat dan efisien
    val preview = remember(retryCount) {
        androidx.camera.core.Preview.Builder()
            .setTargetRotation(android.view.Surface.ROTATION_0)
            .build()
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            // PERBAIKAN: Gunakan PERFORMANCE mode untuk loading yang lebih cepat
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    // PERBAIKAN: ImageCapture dengan konfigurasi yang lebih cepat
    val imageCapture = remember(retryCount) {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(android.view.Surface.ROTATION_0)
            .setJpegQuality(90) // Tingkatkan kualitas sedikit
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .build()
    }

    val executor = remember { ContextCompat.getMainExecutor(context) }

    // PERBAIKAN: Lifecycle observer yang lebih ringan
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> {
                    try {
                        cameraProvider?.unbindAll()
                    } catch (e: Exception) {
                        android.util.Log.e("CameraCapture", "Error during destroy cleanup", e)
                    }
                }
                else -> { /* Handle other events if needed */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                cameraProvider?.unbindAll()
                camera = null
                cameraProvider = null
            } catch (e: Exception) {
                android.util.Log.e("CameraCapture", "Error during dispose cleanup: ${e.message}")
            }
        }
    }

    // PERBAIKAN: LaunchedEffect yang lebih cepat dengan timeout yang lebih pendek
    LaunchedEffect(lifecycleOwner, retryCount) {
        isInitializing = true
        hasError = false

        try {
            // PERBAIKAN: Delay hanya jika retry untuk menghindari loading yang lama
            if (retryCount > 0) {
                kotlinx.coroutines.delay(300L) // Kurangi delay
            }

            val provider = context.getCameraProvider()
            cameraProvider = provider

            // PERBAIKAN: Unbind yang lebih cepat
            try {
                provider.unbindAll()
            } catch (e: Exception) {
                android.util.Log.w("CameraCapture", "Error unbinding: ${e.message}")
            }

            // PERBAIKAN: Delay yang lebih singkat
            kotlinx.coroutines.delay(100L)

            // Validasi kamera tersedia
            val availableCameras = provider.availableCameraInfos
            if (availableCameras.isEmpty()) {
                throw Exception("Tidak ada kamera yang tersedia di perangkat ini")
            }

            // PERBAIKAN: Pilih kamera dengan validasi yang lebih cepat
            val cameraSelector = when {
                provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }
                else -> {
                    throw Exception("Tidak ada kamera yang dapat digunakan")
                }
            }

            // PERBAIKAN: Bind kamera dengan timeout yang lebih pendek
            val boundCamera = try {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                when {
                    e.message?.contains("busy", ignoreCase = true) == true -> {
                        throw Exception("Kamera sedang digunakan aplikasi lain. Tutup aplikasi kamera lain dan coba lagi.")
                    }
                    e.message?.contains("closed", ignoreCase = true) == true -> {
                        throw Exception("Kamera ditutup oleh sistem. Silakan restart aplikasi.")
                    }
                    else -> {
                        throw Exception("Gagal menghubungkan kamera: ${e.localizedMessage ?: e.message}")
                    }
                }
            }

            camera = boundCamera

            // PERBAIKAN: Set surface provider tanpa delay
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // PERBAIKAN: Monitor camera state dengan timeout yang lebih pendek
            var attempts = 0
            val maxAttempts = 10 // Kurangi dari 20 ke 10

            while (attempts < maxAttempts) {
                kotlinx.coroutines.delay(100L) // Kurangi delay

                try {
                    val cameraState = boundCamera.cameraInfo.cameraState.value
                    val cameraStateType = cameraState?.type

                    when (cameraStateType) {
                        androidx.camera.core.CameraState.Type.OPEN -> {
                            hasError = false
                            errorMessage = ""
                            isInitializing = false
                            return@LaunchedEffect
                        }
                        androidx.camera.core.CameraState.Type.CLOSED -> {
                            if (attempts > 5) {
                                throw Exception("Kamera ditutup oleh sistem")
                            }
                        }
                        else -> {
                            // Continue waiting
                        }
                    }
                } catch (e: Exception) {
                    if (attempts > 5) {
                        throw Exception("Gagal memvalidasi status kamera: ${e.localizedMessage}")
                    }
                }

                attempts++
            }

            // Jika sampai di sini, kamera tidak terbuka dalam waktu yang wajar
            throw Exception("Kamera tidak dapat dibuka dalam waktu yang wajar. Silakan coba lagi.")

        } catch (e: Exception) {
            isInitializing = false
            hasError = true

            errorMessage = when {
                e.message?.contains("permission", ignoreCase = true) == true ->
                    "Izin kamera diperlukan untuk menggunakan fitur ini."
                e.message?.contains("busy", ignoreCase = true) == true ||
                e.message?.contains("digunakan", ignoreCase = true) == true ->
                    "Kamera sedang digunakan aplikasi lain. Tutup aplikasi kamera lain dan coba lagi."
                e.message?.contains("closed", ignoreCase = true) == true ||
                e.message?.contains("ditutup", ignoreCase = true) == true ->
                    "Kamera ditutup oleh sistem. Silakan restart aplikasi."
                retryCount >= 2 ->
                    "Kamera gagal inisialisasi setelah beberapa percobaan. Silakan restart aplikasi."
                else -> e.localizedMessage ?: "Gagal menyiapkan kamera. Silakan coba lagi."
            }
        }
    }

    // Container kamera
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasError) {
            // Error state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                androidx.compose.material3.Button(
                    onClick = {
                        retryCount++
                        hasError = false
                        isInitializing = true
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Coba Lagi")
                }
            }
        } else {
            // PERBAIKAN: Camera preview tanpa overlay yang mengganggu
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // PERBAIKAN: Loading indicator yang tidak menghalangi kamera
            if (isInitializing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Menyiapkan kamera...",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // PERBAIKAN: Tombol capture dengan teks Indonesia dan validasi yang lebih baik
        FloatingActionButton(
            onClick = {
                if (!hasError && !isInitializing) {
                    android.util.Log.d("CameraCapture", "📸 Memulai pengambilan foto...")
                    takePicture(
                        imageCapture = imageCapture,
                        executor = executor,
                        onImageCaptured = { bitmap ->
                            android.util.Log.d("CameraCapture", "✅ Foto berhasil diambil, ukuran: ${bitmap.width}x${bitmap.height}")
                            onImageCaptured(bitmap)
                        },
                        onError = { error ->
                            android.util.Log.e("CameraCapture", "❌ Error mengambil foto: ${error.message}")
                            onError(error)
                        }
                    )
                } else {
                    android.util.Log.w("CameraCapture", "⚠️ Kamera belum siap atau ada error")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp),
            containerColor = if (!hasError && !isInitializing)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = Icons.Filled.Camera,
                contentDescription = "Ambil Foto",
                tint = if (!hasError && !isInitializing)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@ExperimentalGetImage
private fun takePicture(
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    try {
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val cameraImage = image.image
                        if (cameraImage == null) {
                            onError(ImageCaptureException(ImageCapture.ERROR_CAPTURE_FAILED, "Sensor data is null", null))
                            return
                        }

                        // PERBAIKAN: Konversi bitmap yang lebih cepat
                        val bitmap = imageProxyToBitmap(image)
                        onImageCaptured(bitmap)
                    } catch (e: Exception) {
                        onError(ImageCaptureException(ImageCapture.ERROR_CAPTURE_FAILED, "Error processing image", e))
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    } catch (e: Exception) {
        onError(ImageCaptureException(ImageCapture.ERROR_CAPTURE_FAILED, "Error starting capture", e))
    }
}

// PERBAIKAN: Fungsi konversi bitmap yang lebih efisien
@ExperimentalGetImage
private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

// Extension function untuk mendapatkan CameraProvider
private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}