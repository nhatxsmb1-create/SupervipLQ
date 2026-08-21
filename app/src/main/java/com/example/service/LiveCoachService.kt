package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
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
import com.example.autocapture.AutoCaptureManager
import com.example.model.CoachStatus
import com.example.model.DangerLevel
import com.example.model.GameState
import com.example.model.ScreenState
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
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private var serviceLifecycleOwner: ServiceLifecycleOwner? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var clockTickerJob: Job? = null

    private val tacticalEngine = TacticalEngine()
    private val visionEngine = VisionAnalysisEngine()
    private var autoCaptureManager: AutoCaptureManager? = null
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

        @Volatile
        var instance: LiveCoachService? = null

        fun triggerInstantScan() {
            instance?.performInstantScan()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        voiceCoach = VoiceCoach(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        if (action == ACTION_STOP) {
            stopServiceAndCleanup()
            return START_NOT_STICKY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildForegroundNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, buildForegroundNotification())
        }
        CoachStateHub.setServiceRunning(true)

        isSimulating = intent?.getBooleanExtra(ACTION_SIMULATE, false) ?: false

        if (Settings.canDrawOverlays(this)) {
            showFloatingOverlay()
        }

        setupMediaProjectionIfAvailable(intent)
        startRealtimeClockTicker()

        return START_STICKY
    }

    private fun setupMediaProjectionIfAvailable(intent: Intent?) {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, projectionResultCode) ?: projectionResultCode
        val resultData = if (intent != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java) ?: projectionResultData
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_RESULT_DATA) ?: projectionResultData
        }

        if (resultCode != 0 && resultData != null) {
            projectionResultCode = resultCode
            projectionResultData = resultData

            if (autoCaptureManager != null) {
                return
            }

            val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            try {
                val mp = mpManager?.getMediaProjection(resultCode, resultData)
                mediaProjection = mp
                Log.d("LiveCoachService", "MediaProjection acquired successfully")

                if (mp != null) {
                    val projectionCallback = object : MediaProjection.Callback() {
                        override fun onStop() {
                            Log.d("LiveCoachService", "MediaProjection stopped by system")
                            autoCaptureManager?.stopCapture()
                            autoCaptureManager = null
                        }
                    }
                    mp.registerCallback(projectionCallback, Handler(Looper.getMainLooper()))

                    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        display
                    } else {
                        @Suppress("DEPRECATION")
                        wm.defaultDisplay
                    }
                    val metrics = resources.displayMetrics
                    val realW = if (display != null) {
                        val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            wm.currentWindowMetrics.bounds
                        } else null
                        bounds?.width() ?: metrics.widthPixels
                    } else metrics.widthPixels

                    val realH = if (display != null) {
                        val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            wm.currentWindowMetrics.bounds
                        } else null
                        bounds?.height() ?: metrics.heightPixels
                    } else metrics.heightPixels

                    val screenLandscapeW = maxOf(realW, realH)
                    val screenLandscapeH = minOf(realW, realH)
                    val realDpi = metrics.densityDpi.coerceAtLeast(240)

                    val acm = AutoCaptureManager(
                        displayWidth = screenLandscapeW,
                        displayHeight = screenLandscapeH,
                        displayDpi = realDpi
                    )
                    autoCaptureManager = acm
                    acm.startCapture(mp)
                    observeAutoCapturedFrames(acm)
                }
            } catch (e: Exception) {
                Log.e("LiveCoachService", "Failed to get MediaProjection", e)
            }
        }
    }

    @Volatile
    private var isAnalyzingFrame = false

    @Volatile
    private var authoritativeMatchTime: Int = 0

    @Volatile
    private var lastOcrTimestamp: Long = 0L

    @Volatile
    private var isCurrentlyInMatch: Boolean = false

    private fun observeAutoCapturedFrames(acm: AutoCaptureManager) {
        serviceScope.launch(Dispatchers.Default) {
            acm.capturedFrames.collect { bitmap ->
                if (isAnalyzingFrame) {
                    bitmap.recycle()
                    return@collect
                }
                isAnalyzingFrame = true
                try {
                    val rawGameState = visionEngine.analyzeFrameToGameState(
                        bitmap = bitmap,
                        captureIntervalMs = acm.getCurrentIntervalMs()
                    )

                    val isMatch = rawGameState.screenState in listOf(
                        ScreenState.IN_MATCH,
                        ScreenState.SCOREBOARD_OPEN,
                        ScreenState.SHOP_OPEN,
                        ScreenState.COMBAT
                    )

                    if (isMatch) {
                        isCurrentlyInMatch = true
                        if (rawGameState.matchTimeSeconds != null && rawGameState.matchTimeSeconds > 0) {
                            authoritativeMatchTime = rawGameState.matchTimeSeconds
                            lastOcrTimestamp = System.currentTimeMillis()
                        }
                    } else {
                        isCurrentlyInMatch = false
                        authoritativeMatchTime = 0
                        lastOcrTimestamp = 0L
                    }

                    val currentCalculatedTime = if (isCurrentlyInMatch) {
                        if (authoritativeMatchTime > 0 && lastOcrTimestamp > 0L) {
                            val elapsed = ((System.currentTimeMillis() - lastOcrTimestamp) / 1000L).toInt()
                            authoritativeMatchTime + elapsed
                        } else {
                            rawGameState.matchTimeSeconds ?: 0
                        }
                    } else {
                        0
                    }

                    val syncedGameState = rawGameState.copy(
                        matchTimeSeconds = currentCalculatedTime,
                        matchActive = isCurrentlyInMatch
                    )

                    CoachStateHub.updateGameState(syncedGameState)
                    acm.setAdaptiveState(syncedGameState.screenState)

                    val currentState = CoachStateHub.tacticalState.value
                    val eval = tacticalEngine.evaluateGameState(syncedGameState, currentState)

                    CoachStateHub.updateState(eval.newState)

                    eval.voiceCallout?.let { phrase ->
                        voiceCoach?.speakCallout(
                            callout = phrase,
                            tag = eval.calloutTag,
                            priority = eval.calloutPriority
                        )
                    }
                } catch (e: Exception) {
                    Log.e("LiveCoachService", "Error analyzing captured frame", e)
                } finally {
                    bitmap.recycle()
                    isAnalyzingFrame = false
                }
            }
        }
    }

    fun performInstantScan() {
        serviceScope.launch(Dispatchers.Default) {
            val currentTactical = CoachStateHub.tacticalState.value
            val currentGameState = CoachStateHub.gameState.value

            val currentTime = if (currentTactical.matchTimeSeconds > 0) currentTactical.matchTimeSeconds else 48
            val simulatedGameState = currentGameState.copy(
                matchActive = true,
                screenState = ScreenState.IN_MATCH,
                matchTimeSeconds = currentTime,
                overallConfidence = 0.95f
            )

            CoachStateHub.updateGameState(simulatedGameState)
            val eval = tacticalEngine.evaluateGameState(simulatedGameState, currentTactical)
            CoachStateHub.updateState(eval.newState)

            eval.voiceCallout?.let { phrase ->
                voiceCoach?.speakCallout(
                    callout = phrase,
                    tag = eval.calloutTag,
                    priority = eval.calloutPriority
                )
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
        windowLayoutParams = params

        val owner = ServiceLifecycleOwner()
        serviceLifecycleOwner = owner

        lateinit var composeView: ComposeView
        composeView = ComposeView(this).apply {
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
                        },
                        onDrag = { dx, dy ->
                            windowLayoutParams?.let { lp ->
                                lp.x += dx
                                lp.y += dy
                                windowManager?.updateViewLayout(composeView, lp)
                            }
                        }
                    )
                }
            }
        }

        overlayFloatingView = composeView
        try {
            windowManager?.addView(composeView, params)
        } catch (e: Exception) {
            Log.e("LiveCoachService", "Failed to add overlay view", e)
        }
    }

    private fun startRealtimeClockTicker() {
        clockTickerJob?.cancel()
        clockTickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                if (isCurrentlyInMatch && authoritativeMatchTime > 0 && lastOcrTimestamp > 0L) {
                    val currentTactical = CoachStateHub.tacticalState.value
                    val elapsed = ((System.currentTimeMillis() - lastOcrTimestamp) / 1000L).toInt()
                    val smoothTime = authoritativeMatchTime + elapsed

                    if (smoothTime != currentTactical.matchTimeSeconds) {
                        val currentGameState = CoachStateHub.gameState.value.copy(
                            matchTimeSeconds = smoothTime,
                            matchActive = true
                        )
                        val eval = tacticalEngine.evaluateGameState(currentGameState, currentTactical)
                        CoachStateHub.updateState(eval.newState)

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
        clockTickerJob?.cancel()
        autoCaptureManager?.stopCapture()
        autoCaptureManager = null
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
        instance = null
        stopServiceAndCleanup()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
