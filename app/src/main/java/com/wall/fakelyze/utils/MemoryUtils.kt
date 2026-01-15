package com.wall.fakelyze.utils

import android.graphics.Bitmap
import android.util.Log
import java.lang.ref.WeakReference

/**
 * Utility class untuk mengelola memory dan mencegah memory leak
 */
object MemoryUtils {
    private const val TAG = "MemoryUtils"

    /**
     * Safely recycle bitmap dengan error handling
     */
    fun safeBitmapRecycle(bitmap: Bitmap?, tag: String = "") {
        try {
            bitmap?.let {
                if (!it.isRecycled) {
                    it.recycle()
                    Log.d(TAG, "Bitmap recycled safely ${if (tag.isNotEmpty()) "($tag)" else ""}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Warning saat recycle bitmap ${if (tag.isNotEmpty()) "($tag)" else ""}: ${e.message}")
        }
    }

    /**
     * Force garbage collection dengan logging
     */
    fun forceGarbageCollection(reason: String = "") {
        try {
            System.gc()
            Log.d(TAG, "Garbage collection forced ${if (reason.isNotEmpty()) "($reason)" else ""}")
        } catch (e: Exception) {
            Log.w(TAG, "Error during garbage collection: ${e.message}")
        }
    }

    /**
     * Get memory info untuk debugging
     */
    fun getMemoryInfo(): String {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val availableMemory = maxMemory - usedMemory

            "Memory - Used: ${formatBytes(usedMemory)}, Available: ${formatBytes(availableMemory)}, Max: ${formatBytes(maxMemory)}"
        } catch (e: Exception) {
            "Memory info unavailable: ${e.message}"
        }
    }

    /**
     * Format bytes to human readable format
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
            bytes >= 1024 -> "${bytes / 1024}KB"
            else -> "${bytes}B"
        }
    }

    /**
     * Check if memory is low
     */
    fun isMemoryLow(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val usagePercentage = (usedMemory.toDouble() / maxMemory.toDouble()) * 100

            usagePercentage > 80.0 // Consider memory low if usage > 80%
        } catch (e: Exception) {
            Log.w(TAG, "Error checking memory status: ${e.message}")
            false
        }
    }

    /**
     * Log memory status
     */
    fun logMemoryStatus(context: String = "") {
        Log.d(TAG, "${if (context.isNotEmpty()) "$context - " else ""}${getMemoryInfo()}")
    }
}

/**
 * Extension function untuk WeakReference
 */
fun <T> T.toWeakReference(): WeakReference<T> = WeakReference(this)
