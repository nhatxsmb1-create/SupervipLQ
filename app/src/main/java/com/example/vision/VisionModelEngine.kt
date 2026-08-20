package com.example.vision

import android.graphics.Bitmap
import com.example.detection.DetectedUIComponent
import com.example.model.ScreenState

data class VisionModelDetection(
    val categoryName: String,
    val confidence: Float,
    val detectedComponent: DetectedUIComponent?
)

interface IVisionModel {
    suspend fun classifyScreenState(bitmap: Bitmap): ScreenState
    suspend fun detectIcons(bitmap: Bitmap): List<VisionModelDetection>
    fun close()
}

/**
 * Placeholder / Rule-based fallback implementation of local Computer Vision model interface.
 * Connects to future TensorFlow Lite (.tflite) models when model files are present.
 */
class TFLiteVisionModelPlaceholder : IVisionModel {

    override suspend fun classifyScreenState(bitmap: Bitmap): ScreenState {
        if (bitmap.isRecycled) return ScreenState.UNKNOWN
        // Local heuristic vision fallback until local .tflite model is bundled
        return ScreenState.IN_MATCH
    }

    override suspend fun detectIcons(bitmap: Bitmap): List<VisionModelDetection> {
        if (bitmap.isRecycled) return emptyList()
        return emptyList()
    }

    override fun close() {
        // Release TFLite interpreter resources if initialized
    }
}
