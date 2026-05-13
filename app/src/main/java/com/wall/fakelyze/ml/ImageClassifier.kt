package com.wall.fakelyze.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.lang.ref.WeakReference

class ImageClassifier(context: Context) {

    companion object {
        private const val TAG = "ImageClassifier"
        private const val MODEL_FILENAME = "Mobilenetv2_Fix.tflite"
        private const val IMAGE_SIZE = 224
        private const val NUM_CHANNELS = 3
        private const val PIXEL_SIZE = 4 // Float32 = 4 bytes
    }

    // Use WeakReference to prevent memory leak
    private val contextRef = WeakReference(context.applicationContext)

    private var interpreter: Interpreter? = null
    private var imageSize = IMAGE_SIZE
    private var labels: List<String> = listOf("FAKE", "REAL")

    // Konfigurasi untuk MobileNetV2
    private val numChannels = NUM_CHANNELS
    private val pixelSize = PIXEL_SIZE
    private var inputShape: IntArray? = null
    private var outputShape: IntArray? = null

    // Flag to track if resources are closed
    private var isClosed = false
    private var setupAttempted = false
    private var isModelReady = false
    private var useDummyModel = false // Fallback untuk development

    init {
        try {
            setupModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error during ImageClassifier initialization", e)
            // Gunakan dummy model sebagai fallback
            setupDummyModel()
        }
    }

    private fun setupModel() {
        if (setupAttempted) return
        setupAttempted = true

        try {
            val context = contextRef.get()
            if (context == null) {
                Log.e(TAG, "Context is null, cannot setup model")
                setupDummyModel()
                return
            }

            // Load model dari direktori ml dalam assets
            val modelFile = loadModelFromMlDirectory(context)
            if (modelFile == null) {
                Log.e(TAG, "Failed to load model file from ml directory")
                setupDummyModel()
                return
            }

            // PERBAIKAN: Setup TensorFlow Lite interpreter dengan optimasi performa
            val options = Interpreter.Options().apply {
                setNumThreads(4) // Tingkatkan thread untuk performa yang lebih baik
                setUseNNAPI(true) // Enable NNAPI untuk hardware acceleration
                setAllowFp16PrecisionForFp32(true) // Enable optimization
                setAllowBufferHandleOutput(true) // Optimasi buffer handling
            }

            interpreter = Interpreter(modelFile, options)

            // Get input dan output shapes
            inputShape = interpreter?.getInputTensor(0)?.shape()
            outputShape = interpreter?.getOutputTensor(0)?.shape()

            if (inputShape != null && inputShape!!.size >= 4) {
                imageSize = inputShape!![1] // Biasanya [batch, height, width, channels]
                Log.d(TAG, "Model input shape: ${inputShape!!.contentToString()}")
                Log.d(TAG, "Model output shape: ${outputShape!!.contentToString()}")
            }

            isModelReady = true
            useDummyModel = false
            Log.d(TAG, "Model MobileNetV2 berhasil dimuat dari direktori ml")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up model", e)
            setupDummyModel()
        }
    }

    private fun setupDummyModel() {
        try {
            Log.i(TAG, "Menggunakan dummy model untuk demo - hasil akan berupa simulasi")
            isModelReady = true
            useDummyModel = true
            inputShape = intArrayOf(1, IMAGE_SIZE, IMAGE_SIZE, NUM_CHANNELS)
            outputShape = intArrayOf(1, 2)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up dummy model", e)
        }
    }

    private fun loadModelFromAssets(context: Context): ByteBuffer? {
        return try {
            val assetManager = context.assets
            val inputStream = assetManager.open(MODEL_FILENAME)
            val fileChannel = (inputStream as? FileInputStream)?.channel

            if (fileChannel != null) {
                val modelBuffer = fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    fileChannel.size()
                )
                inputStream.close()
                modelBuffer
            } else {
                // Fallback untuk non-FileInputStream
                val bytes = inputStream.readBytes()
                inputStream.close()

                val buffer = ByteBuffer.allocateDirect(bytes.size)
                buffer.order(ByteOrder.nativeOrder())
                buffer.put(bytes)
                buffer.rewind()
                buffer
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model from assets", e)
            null
        }
    }

    private fun loadModelFromMlDirectory(context: Context): ByteBuffer? {
        return try {
            // Coba akses folder ml di assets
            val modelPath = "ml/$MODEL_FILENAME"
            val assetManager = context.assets
            val inputStream = assetManager.open(modelPath)

            // PERBAIKAN: Baca file model dengan buffer yang optimal
            val bytes = inputStream.readBytes()
            inputStream.close()

            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()
            buffer
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model from ml directory", e)
            // Fallback ke assets root
            loadModelFromAssets(context)
        }
    }

    // PERBAIKAN 3: Fungsi classify yang lebih cepat dengan optimasi
    suspend fun classify(bitmap: Bitmap): Pair<String, Float> = withContext(Dispatchers.Default) {
        if (isClosed) {
            Log.w(TAG, "ImageClassifier sudah ditutup")
            return@withContext Pair("UNKNOWN", 0.0f)
        }

        if (!isModelReady) {
            Log.w(TAG, "Model belum siap")
            return@withContext Pair("UNKNOWN", 0.0f)
        }

        try {
            // ✅ CEK KUALITAS GAMBAR
            if (!checkImageQuality(bitmap)) {
                Log.w(TAG, "Image quality check failed")
                return@withContext Pair("LOW_QUALITY", 0.0f)
            }

            if (useDummyModel) {
                // Dummy model dengan delay yang lebih singkat
                kotlinx.coroutines.delay(500L) // Kurangi delay dari 1000ms ke 500ms

                // Simulasi hasil berdasarkan properties gambar
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

                // Analisis sederhana untuk demo
                val avgBrightness = pixels.map { pixel ->
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    (r + g + b) / 3
                }.average()

                val confidence = when {
                    avgBrightness > 150 -> 0.85f
                    avgBrightness > 100 -> 0.75f
                    else -> 0.65f
                }

                val result = if (avgBrightness > 120) "REAL" else "FAKE"
                Log.d(TAG, "Dummy model result: $result dengan confidence: $confidence")
                return@withContext Pair(result, confidence)
            }

            // ✅ PREPROCESSING DENGAN ASPECT RATIO
            Log.d(TAG, "Original bitmap size: ${bitmap.width}x${bitmap.height}")
            val inputBuffer = preprocessImageWithAspectRatio(bitmap)

            // ✅ SIAPKAN OUTPUT BUFFER
            val outputBuffer = TensorBuffer.createFixedSize(outputShape!!, DataType.FLOAT32)

            // ✅ JALANKAN INFERENCE
            val startTime = System.currentTimeMillis()
            interpreter?.run(inputBuffer, outputBuffer.buffer)
            val inferenceTime = System.currentTimeMillis() - startTime

            Log.d(TAG, "Inference time: ${inferenceTime}ms")

            // ✅ INTERPRETASI HASIL DENGAN PERBAIKAN
            val result = interpretOutput(outputBuffer.floatArray)

            Log.d(TAG, "Classification result: ${result.first} dengan confidence: ${(result.second * 100).toInt()}%")

            return@withContext result

        } catch (e: Exception) {
            Log.e(TAG, "Error during classification", e)
            e.printStackTrace()
            return@withContext Pair("ERROR", 0.0f)
        }
    }

    // PERBAIKAN 2: Tambahkan fungsi untuk mengecek kualitas gambar
    private fun checkImageQuality(bitmap: Bitmap): Boolean {
        // Cek resolusi minimum
        if (bitmap.width < 100 || bitmap.height < 100) {
            Log.w(TAG, "Image resolution too low: ${bitmap.width}x${bitmap.height}")
            return false
        }

        // Cek apakah gambar terlalu gelap atau terang
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val avgBrightness = pixels.map { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            (r + g + b) / 3
        }.average()

        Log.d(TAG, "Average brightness: $avgBrightness")

        if (avgBrightness < 20 || avgBrightness > 235) {
            Log.w(TAG, "Image brightness out of optimal range")
            return false
        }

        return true
    }

    // PERBAIKAN 4: Preprocessing dengan Aspect Ratio
    private fun preprocessImageWithAspectRatio(bitmap: Bitmap): ByteBuffer {
        // ✅ PERTAHANKAN ASPECT RATIO dengan center crop
        val scaledBitmap = if (bitmap.width != bitmap.height) {
            val size = Math.min(bitmap.width, bitmap.height)
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2
            Bitmap.createBitmap(bitmap, x, y, size, size)
        } else {
            bitmap
        }

        Log.d(TAG, "Scaled bitmap size (center crop): ${scaledBitmap.width}x${scaledBitmap.height}")

        val resizedBitmap = Bitmap.createScaledBitmap(
            scaledBitmap,
            imageSize,
            imageSize,
            true
        )

        Log.d(TAG, "Resized bitmap size: ${resizedBitmap.width}x${resizedBitmap.height}")

        val inputBuffer = ByteBuffer.allocateDirect(
            1 * imageSize * imageSize * numChannels * pixelSize
        )
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSize * imageSize)
        resizedBitmap.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

        for (pixel in pixels) {
            // ✅ NORMALISASI SESUAI TRAINING (0-1)
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        // Cleanup
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        if (resizedBitmap != scaledBitmap) {
            resizedBitmap.recycle()
        }

        return inputBuffer
    }

    // PERBAIKAN: Preprocessing yang lebih efisien (fungsi lama - bisa dihapus jika tidak digunakan)
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(1 * imageSize * imageSize * numChannels * pixelSize)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(imageSize * imageSize)
        bitmap.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

        var pixelIndex = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val pixel = pixels[pixelIndex++]

                // Normalisasi pixel yang lebih efisien
                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        return inputBuffer
    }

    // PERBAIKAN: Interpretasi output yang lebih cepat
    private fun interpretOutput(outputArray: FloatArray): Pair<String, Float> {
        if (outputArray.size < 2) {
            Log.w(TAG, "Output array size insufficient: ${outputArray.size}")
            return Pair("UNKNOWN", 0.0f)
        }

        // Model output: [FAKE_probability, REAL_probability]
        val fakeProb = outputArray[0]
        val realProb = outputArray[1]

        // Log untuk debugging
        Log.d(TAG, "Raw output - FAKE: $fakeProb, REAL: $realProb")

        // Cek apakah output sudah dalam bentuk probabilitas (sum ≈ 1.0)
        val sum = fakeProb + realProb
        Log.d(TAG, "Probability sum: $sum")

        // Jika sum tidak mendekati 1.0, kemungkinan perlu softmax
        val (normalizedFake, normalizedReal) = if (sum < 0.9f || sum > 1.1f) {
            // Apply softmax
            applySoftmax(floatArrayOf(fakeProb, realProb))
        } else {
            // Sudah probabilitas
            Pair(fakeProb, realProb)
        }

        Log.d(TAG, "Normalized - FAKE: $normalizedFake, REAL: $normalizedReal")

        // Tentukan hasil berdasarkan probabilitas tertinggi
        val (result, confidence) = if (normalizedReal > normalizedFake) {
            Pair("REAL", normalizedReal)
        } else {
            Pair("FAKE", normalizedFake)
        }

        // Confidence sudah dalam range 0-1, konversi ke persentase jika perlu
        val confidencePercentage = (confidence * 100).coerceIn(0f, 100f)

        Log.d(TAG, "Final result: $result with confidence: $confidencePercentage%")

        return Pair(result, confidence)
    }

    // Fungsi helper untuk softmax
    private fun applySoftmax(logits: FloatArray): Pair<Float, Float> {
        val maxLogit = logits.maxOrNull() ?: 0f
        val expValues = logits.map { kotlin.math.exp((it - maxLogit).toDouble()).toFloat() }
        val sumExp = expValues.sum()

        return Pair(
            expValues[0] / sumExp,
            expValues[1] / sumExp
        )
    }

    // PERBAIKAN: Cleanup resources yang lebih baik
    fun close() {
        if (isClosed) return

        try {
            interpreter?.close()
            interpreter = null
            isClosed = true
            isModelReady = false
            Log.d(TAG, "ImageClassifier resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ImageClassifier", e)
        }
    }

    // Fungsi untuk mengecek apakah model siap digunakan
    fun isReady(): Boolean = isModelReady && !isClosed

    // Fungsi untuk mendapatkan informasi model
    fun getModelInfo(): String {
        return if (useDummyModel) {
            "Dummy Model (Demo Mode)"
        } else {
            "MobileNetV2 Model - Input: ${inputShape?.contentToString()} Output: ${outputShape?.contentToString()}"
        }
    }
}
