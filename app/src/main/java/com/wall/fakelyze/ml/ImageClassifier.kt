package com.wall.fakelyze.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.File
import java.io.FileOutputStream

class ImageClassifier(private val context: Context) {

    private var classifier: ImageClassifier? = null

    init {
        setupClassifier()
    }

    private fun setupClassifier() {
        try {
            val baseOptions = BaseOptions.builder()
                .setNumThreads(4)
                .build()

            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(2)
                .setScoreThreshold(0.3f)
                .build()

            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "mobilenet_v2_ai_detector.tflite",
                options
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up the image classifier", e)
        }
    }

    suspend fun classifyImage(bitmap: Bitmap): ClassificationResult = withContext(Dispatchers.Default) {
        try {
            // Preprocess the image: resize to 224x224 (MobileNetV2 input size)
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .build()

            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

            // Ensure classifier is initialized
            if (classifier == null) {
                setupClassifier()
            }

            // Run inference
            val results = classifier?.classify(tensorImage)

            // Process results
            if (results != null && results.isNotEmpty() && results[0].categories.isNotEmpty()) {
                val categories = results[0].categories

                // Assuming the model outputs two classes: real or ai generated
                val aiGeneratedCategory = categories.find { it.label.contains("ai", ignoreCase = true) }
                val realCategory = categories.find { it.label.contains("real", ignoreCase = true) }

                val isAIGenerated = if (aiGeneratedCategory != null && realCategory != null) {
                    aiGeneratedCategory.score > realCategory.score
                } else {
                    aiGeneratedCategory != null
                }

                val confidenceScore = aiGeneratedCategory?.score ?: 0f

                // Save the image for history
                val (imagePath, thumbnailPath) = saveBitmapToFile(bitmap)

                ClassificationResult.Success(
                    isAIGenerated = isAIGenerated,
                    confidenceScore = confidenceScore,
                    imagePath = imagePath,
                    thumbnailPath = thumbnailPath
                )
            } else {
                ClassificationResult.Error("No classification results found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error classifying image", e)
            ClassificationResult.Error("Error classifying image: ${e.message}")
        }
    }

    private suspend fun saveBitmapToFile(bitmap: Bitmap): Pair<String, String> = withContext(
        Dispatchers.IO) {
        val timeStamp = System.currentTimeMillis()
        val imageDir = File(context.filesDir, "images")
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }

        // Save full-size image
        val imageFile = File(imageDir, "image_$timeStamp.jpg")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // Create and save thumbnail
        val thumbnailSize = 120
        val thumbBitmap = Bitmap.createScaledBitmap(bitmap, thumbnailSize, thumbnailSize, true)
        val thumbnailFile = File(imageDir, "thumb_$timeStamp.jpg")
        FileOutputStream(thumbnailFile).use { out ->
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        Pair(imageFile.absolutePath, thumbnailFile.absolutePath)
    }

    fun close() {
        classifier?.close()
        classifier = null
    }

    companion object {
        private const val TAG = "ImageClassifier"
        private const val IMAGE_SIZE = 224
    }
}

sealed class ClassificationResult {
    data class Success(
        val isAIGenerated: Boolean,
        val confidenceScore: Float,
        val imagePath: String,
        val thumbnailPath: String
    ) : ClassificationResult()

    data class Error(val errorMessage: String) : ClassificationResult()
}