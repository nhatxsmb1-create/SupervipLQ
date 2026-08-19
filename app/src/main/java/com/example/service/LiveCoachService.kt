package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.model.DetectedScreenMode
import com.example.tactical.TacticalEngine
import com.example.ui.theme.ArenaCoachTheme
import com.example.vision.VisionAnalysisEngine
import com.example.voice.VoiceCoach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class ServiceLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun destroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class LiveCoachService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayFloatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var serviceLifecycleOwner: ServiceLifecycleOwner? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var analysisLoopJob: Job? = null

    private val tacticalEngine = TacticalEngine()
    private val visionEngine = VisionAnalysisEngine()
    private var voiceCoach: VoiceCoach? = null

    private var mediaProjection: MediaProjection? = null
    private var isSimulating = false

    companion object {
        const val CHANNEL_ID = "arena_coach_live_channel"
        const val NOTIFICATION_ID = 9001

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_SIMULATE = "com.example.service.ACTION_SIMULATE"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        var projectionResultCode: Int = 0
        var projectionResultData: Intent? = null
    }

    override fun onCreate() {
        super.onCreate()
        voiceCoach = VoiceCoach(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        if (action == ACTION_STOP) {
            stopServiceAndCleanup()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        CoachStateHub.setServiceRunning(true)

        isSimulating = intent?.getBooleanExtra(ACTION_SIMULATE, false) ?: false

        if (Settings.canDrawOverlays(this)) {
            showFloatingOverlay()
        }

        setupMediaProjectionIfAvailable(intent)
        startAdaptiveCoachLoop()

        return START_STICKY
    }

    private fun setupMediaProjectionIfAvailable(intent: Intent?) {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, projectionResultCode) ?: projectionResultCode
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA) ?: projectionResultData

        if (resultCode != 0 && resultData != null) {
            val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            try {
                mediaProjection = mpManager?.getMediaProjection(resultCode, resultData)
                Log.d("LiveCoachService", "MediaProjection acquired successfully")
            } catch (e: Exception) {
                Log.e("LiveCoachService", "Failed to get MediaProjection", e)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingOverlay() {
        if (overlayFloatingView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 180
        }
        layoutParams = params

        val owner = ServiceLifecycleOwner()
        serviceLifecycleOwner = owner

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)

            setContent {
                ArenaCoachTheme(darkTheme = true) {
                    CoachOverlayScreen(
                        onCloseClick = { stopServiceAndCleanup() },
                        onToggleExpand = { CoachStateHub.toggleOverlayExpanded() },
                        onToggleVoiceMute = {
                            CoachStateHub.toggleVoiceMute()
                            val muted = CoachStateHub.tacticalState.value.isVoiceMuted
                            voiceCoach?.setMuted(muted)
                        }
                    )
                }
            }
        }

        // Make floating overlay touch draggable
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        composeView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(composeView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val totalMove = abs(event.rawX - initialTouchX) + abs(event.rawY - initialTouchY)
                    if (totalMove < 10) {
                        composeView.performClick()
                    }
                    true
                }
                else -> false
            }
        }

        overlayFloatingView = composeView
        try {
            windowManager?.addView(composeView, params)
        } catch (e: Exception) {
            Log.e("LiveCoachService", "Failed to add overlay view", e)
        }
    }

    private fun startAdaptiveCoachLoop() {
        analysisLoopJob?.cancel()
        analysisLoopJob = serviceScope.launch {
            var elapsedSeconds = 0

            while (isActive) {
                val currentState = CoachStateHub.tacticalState.value

                // Determine interval based on game state (Idle: 5s, Combat: 1s, Scoreboard/Shop: immediate)
                val delayMs = when (currentState.detectedUIMode) {
                    DetectedScreenMode.COMBAT -> 1000L
                    DetectedScreenMode.SCOREBOARD_OPEN -> 800L
                    DetectedScreenMode.SHOP_OPEN -> 800L
                    DetectedScreenMode.MINIMAP_ALERT -> 2000L
                    DetectedScreenMode.IDLE -> 5000L
                }

                delay(delayMs)
                elapsedSeconds += (delayMs / 1000).toInt().coerceAtLeast(1)

                // Tactical Rule-based Evaluation
                val eval = tacticalEngine.evaluate(
                    matchTimeSeconds = currentState.matchTimeSeconds + (delayMs / 1000).toInt().coerceAtLeast(1),
                    goldDiff = currentState.teamGoldDiff,
                    allyKills = currentState.allyKills,
                    enemyKills = currentState.enemyKills,
                    allyTowers = currentState.allyTowers,
                    enemyTowers = currentState.enemyTowers,
                    detectedMode = currentState.detectedUIMode
                )

                // Update shared state
                CoachStateHub.updateState(eval.newState)

                // Trigger voice callout if applicable
                eval.voiceCallout?.let { phrase ->
                    voiceCoach?.speakCallout(
                        callout = phrase,
                        tag = eval.calloutTag,
                        priority = eval.calloutPriority
                    )
                }
            }
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, LiveCoachService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(com.example.R.string.service_notification_title))
            .setContentText(getString(com.example.R.string.service_notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(com.example.R.string.service_stop_action), stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cửa Sổ Nổi Trợ Lý Liên Quân",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị trạng thái chiến thuật và cảnh báo trực tiếp trong trận"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopServiceAndCleanup() {
        analysisLoopJob?.cancel()
        mediaProjection?.stop()
        mediaProjection = null

        overlayFloatingView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {}
        }
        overlayFloatingView = null

        serviceLifecycleOwner?.destroy()
        serviceLifecycleOwner = null

        voiceCoach?.release()
        voiceCoach = null
        visionEngine.close()

        CoachStateHub.setServiceRunning(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopServiceAndCleanup()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
