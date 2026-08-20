package com.example.autocapture

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

/**
 * Lightweight image change detector.
 * Compares incoming bitmap against previous frame by sampling a sparse grid.
 * Avoids expensive OCR / CV processing when the screen hasn't changed.
 */
class FrameChangeDetector(
    private val sampleGridRows: Int = 16,
    private val sampleGridCols: Int = 16,
    private val changeThresholdPercent: Float = 2.5f
) {
    private var lastSampledPixels: IntArray? = null

    data class ChangeResult(
        val hasChanged: Boolean,
        val differencePercent: Float
    )

    fun checkFrameChange(bitmap: Bitmap): ChangeResult {
        if (bitmap.isRecycled) {
            return ChangeResult(hasChanged = false, differencePercent = 0f)
        }

        val width = bitmap.width
        val height = bitmap.height
        val totalSamples = sampleGridRows * sampleGridCols
        val currentSampledPixels = IntArray(totalSamples)

        val stepX = (width / sampleGridCols).coerceAtLeast(1)
        val stepY = (height / sampleGridRows).coerceAtLeast(1)

        var sampleIndex = 0
        for (r in 0 until sampleGridRows) {
            for (c in 0 until sampleGridCols) {
                val px = (c * stepX + stepX / 2).coerceIn(0, width - 1)
                val py = (r * stepY + stepY / 2).coerceIn(0, height - 1)
                currentSampledPixels[sampleIndex++] = bitmap.getPixel(px, py)
            }
        }

        val previous = lastSampledPixels
        if (previous == null || previous.size != totalSamples) {
            lastSampledPixels = currentSampledPixels
            return ChangeResult(hasChanged = true, differencePercent = 100f)
        }

        var totalLumaDiff = 0L
        for (i in 0 until totalSamples) {
            val p1 = previous[i]
            val p2 = currentSampledPixels[i]

            val r1 = Color.red(p1)
            val g1 = Color.green(p1)
            val b1 = Color.blue(p1)

            val r2 = Color.red(p2)
            val g2 = Color.green(p2)
            val b2 = Color.blue(p2)

            val luma1 = (r1 * 299 + g1 * 587 + b1 * 114) / 1000
            val luma2 = (r2 * 299 + g2 * 587 + b2 * 114) / 1000

            totalLumaDiff += abs(luma1 - luma2)
        }

        val maxPossibleDiff = totalSamples * 255L
        val diffPercent = (totalLumaDiff.toFloat() / maxPossibleDiff.toFloat()) * 100f

        val changed = diffPercent >= changeThresholdPercent
        if (changed) {
            lastSampledPixels = currentSampledPixels
        }

        return ChangeResult(
            hasChanged = changed,
            differencePercent = diffPercent
        )
    }

    fun reset() {
        lastSampledPixels = null
    }
}
