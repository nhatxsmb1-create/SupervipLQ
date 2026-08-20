package com.example.autocapture

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.example.model.ScreenState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Manages automatic MediaProjection screen capture with adaptive frame rate and resource safety.
 */
class AutoCaptureManager(
    private val displayWidth: Int = 1280,
    private val displayHeight: Int = 720,
    private val displayDpi: Int = 240
) {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private val _capturedFrames = MutableSharedFlow<Bitmap>(extraBufferCapacity = 2)
    val capturedFrames: SharedFlow<Bitmap> = _capturedFrames.asSharedFlow()

    private var isCapturing = false
    private var lastCaptureTimeMs = 0L

    @Volatile
    private var currentIntervalMs: Long = 3000L // Default to low frequency outside match

    fun startCapture(projection: MediaProjection) {
        if (isCapturing) return
        this.mediaProjection = projection

        backgroundThread = HandlerThread("AutoCaptureThread").apply { start() }
        backgroundHandler = Handler(backgroundThread!!.looper)

        setupImageReader()
        isCapturing = true
        Log.d("AutoCaptureManager", "Screen capture started ($displayWidth x $displayHeight)")
    }

    @SuppressLint("WrongConstant")
    private fun setupImageReader() {
        val reader = ImageReader.newInstance(displayWidth, displayHeight, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        reader.setOnImageAvailableListener({ imageReader ->
            val now = System.currentTimeMillis()
            if (now - lastCaptureTimeMs < currentIntervalMs) {
                // Drop frame according to adaptive frame rate
                imageReader.acquireLatestImage()?.close()
                return@setOnImageAvailableListener
            }

            var image: Image? = null
            try {
                image = imageReader.acquireLatestImage()
                if (image != null) {
                    val bitmap = imageToBitmap(image)
                    if (bitmap != null) {
                        lastCaptureTimeMs = now
                        _capturedFrames.tryEmit(bitmap)
                    }
                }
            } catch (e: Exception) {
                Log.e("AutoCaptureManager", "Error processing captured frame", e)
            } finally {
                image?.close()
            }
        }, backgroundHandler)

        try {
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ArenaCoachDisplay",
                displayWidth,
                displayHeight,
                displayDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                backgroundHandler
            )
        } catch (e: Exception) {
            Log.e("AutoCaptureManager", "Failed to create VirtualDisplay", e)
        }
    }

    /**
     * Adaptively adjust capture interval based on screen state and game events.
     */
    fun setAdaptiveState(screenState: ScreenState) {
        val targetInterval = when (screenState) {
            ScreenState.OUTSIDE_GAME -> 4000L
            ScreenState.GAME_MENU, ScreenState.LOADING -> 2500L
            ScreenState.IN_MATCH -> 1000L
            ScreenState.SCOREBOARD_OPEN, ScreenState.SHOP_OPEN -> 400L // High frequency for scoreboard/shop
            ScreenState.COMBAT -> 500L
            ScreenState.MATCH_END -> 3000L
            ScreenState.UNKNOWN -> 2000L
        }
        currentIntervalMs = targetInterval
    }

    fun getCurrentIntervalMs(): Long = currentIntervalMs

    private fun imageToBitmap(image: Image): Bitmap? {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * displayWidth

        val bitmap = Bitmap.createBitmap(
            displayWidth + rowPadding / pixelStride,
            displayHeight,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        // Crop padding if any
        return if (rowPadding > 0) {
            val cropped = Bitmap.createBitmap(bitmap, 0, 0, displayWidth, displayHeight)
            bitmap.recycle()
            cropped
        } else {
            bitmap
        }
    }

    fun stopCapture() {
        isCapturing = false
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            backgroundThread?.quitSafely()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: Exception) {
            Log.e("AutoCaptureManager", "Error stopping capture", e)
        }
        Log.d("AutoCaptureManager", "Screen capture stopped")
    }
}
