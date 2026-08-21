package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ArenaCoachApp
import com.example.model.DangerLevel
import com.example.model.DetectedScreenMode
import com.example.model.MatchRecord
import com.example.model.MatchResult
import com.example.model.ObjectiveTarget
import com.example.model.TacticalState
import com.example.model.TacticalLogEntity
import com.example.service.CoachStateHub
import com.example.service.LiveCoachService
import com.example.tactical.TacticalEngine
import com.example.voice.VoiceCoach
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CoachViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArenaCoachApp
    private val matchRepository = app.matchRepository
    private val preferencesRepository = app.userPreferencesRepository

    val tacticalState: StateFlow<TacticalState> = CoachStateHub.tacticalState
    val isServiceRunning: StateFlow<Boolean> = CoachStateHub.isServiceRunning
    val isOverlayExpanded: StateFlow<Boolean> = CoachStateHub.isOverlayExpanded

    val coachSettings = preferencesRepository.coachSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.data.repository.CoachSettings()
    )

    private val voiceCoach = VoiceCoach(application)
    private val tacticalEngine = TacticalEngine()
    private var simulationJob: kotlinx.coroutines.Job? = null
    val isSimulatingFlow = kotlinx.coroutines.flow.MutableStateFlow(false)

    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(getApplication())
    }

    fun startCoachService(isSimulation: Boolean = false) {
        val context = getApplication<Application>()
        val intent = Intent(context, LiveCoachService::class.java).apply {
            action = LiveCoachService.ACTION_START
            putExtra(LiveCoachService.ACTION_SIMULATE, isSimulation)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopCoachService() {
        stopMatchSimulation()
        val context = getApplication<Application>()
        val intent = Intent(context, LiveCoachService::class.java).apply {
            action = LiveCoachService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun toggleMatchSimulation() {
        if (isSimulatingFlow.value) {
            stopMatchSimulation()
        } else {
            startMatchSimulation()
        }
    }

    fun startMatchSimulation() {
        simulationJob?.cancel()
        isSimulatingFlow.value = true

        // Ensure service overlay is visible if overlay permission is granted
        if (canDrawOverlays() && !isServiceRunning.value) {
            startCoachService(isSimulation = true)
        }

        simulationJob = viewModelScope.launch {
            var currentSec = 0
            while (isSimulatingFlow.value) {
                jumpToMatchTime(currentSec, triggerVoice = (currentSec in listOf(30, 48, 90, 120, 240, 480, 900)))
                kotlinx.coroutines.delay(1000L)
                currentSec++
                if (currentSec > 1200) currentSec = 0
            }
        }
    }

    fun stopMatchSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        isSimulatingFlow.value = false
    }

    fun jumpToMatchTime(seconds: Int, triggerVoice: Boolean = true) {
        val currentTactical = CoachStateHub.tacticalState.value
        val goldDiff = when {
            seconds < 120 -> 0
            seconds in 120..400 -> 800
            seconds in 401..700 -> 2200
            seconds in 701..900 -> 4500
            else -> 6000
        }
        val allyKills = (seconds / 100).coerceAtLeast(0)
        val enemyKills = (seconds / 140).coerceAtLeast(0)

        val newGameState = com.example.model.GameState(
            matchActive = true,
            screenState = com.example.model.ScreenState.IN_MATCH,
            matchTimeSeconds = seconds,
            allyKills = allyKills,
            enemyKills = enemyKills,
            goldDifference = goldDiff,
            overallConfidence = 0.95f
        )
        CoachStateHub.updateGameState(newGameState)
        val eval = tacticalEngine.evaluateGameState(newGameState, currentTactical)
        CoachStateHub.updateState(eval.newState)

        if (triggerVoice) {
            eval.voiceCallout?.let { phrase ->
                voiceCoach.speakCallout(
                    callout = phrase,
                    tag = eval.calloutTag,
                    priority = eval.calloutPriority
                )
            }
        }
    }

    fun selectPresetScenario(index: Int) {
        CoachStateHub.applyPresetScenario(index)
        val state = CoachStateHub.tacticalState.value
        // Test voice callout for scenario in Vietnamese
        when (index) {
            0 -> voiceCoach.speakCallout("Kẻ địch biến mất ở Sông", "danger_gank", 2, forceIgnoreCooldown = true)
            1 -> voiceCoach.speakCallout("Tránh giao tranh - Đang thua tiền", "danger_gold", 3, forceIgnoreCooldown = true)
            2 -> voiceCoach.speakCallout("Lên Đao Truy Hồn khắc chế hồi máu", "item_counter", 2, forceIgnoreCooldown = true)
            3 -> voiceCoach.speakCallout("Ăn Tà Thần Caesar", "objective_slayer", 2, forceIgnoreCooldown = true)
            4 -> voiceCoach.speakCallout("Thủ Trụ Siêu Cấp và Nhà Chính", "base_defend", 3, forceIgnoreCooldown = true)
        }
    }

    fun updateGoldDiff(newDiff: Int) {
        val current = tacticalState.value
        val eval = tacticalEngine.evaluate(
            currentState = current,
            matchTimeSeconds = current.matchTimeSeconds,
            goldDiff = newDiff,
            allyKills = current.allyKills,
            enemyKills = current.enemyKills,
            allyTowers = current.allyTowers,
            enemyTowers = current.enemyTowers,
            detectedMode = current.detectedUIMode
        )
        CoachStateHub.updateState(eval.newState)
        eval.voiceCallout?.let { voiceCoach.speakCallout(it, eval.calloutTag, eval.calloutPriority) }
    }

    fun startManualAnalysis() {
        // Auto-capture performs analysis continuously
    }

    fun stopManualAnalysis() {
        CoachStateHub.resetToIdle()
    }

    fun triggerVoiceCallout(phrase: String) {
        voiceCoach.speakCallout(phrase, phrase, 3, forceIgnoreCooldown = true)
    }

    fun toggleVoiceMute() {
        CoachStateHub.toggleVoiceMute()
        voiceCoach.setMuted(CoachStateHub.tacticalState.value.isVoiceMuted)
    }

    fun toggleOverlayExpanded() {
        CoachStateHub.toggleOverlayExpanded()
    }

    fun saveCurrentMatchToHistory(heroName: String = "Florentino", isWin: Boolean = true) {
        viewModelScope.launch {
            val state = tacticalState.value
            val match = MatchRecord(
                heroUsed = heroName,
                heroRole = "Đấu Sĩ / Đường Tà Thần",
                result = if (isWin) MatchResult.VICTORY else MatchResult.DEFEAT,
                durationSeconds = state.matchTimeSeconds,
                finalKDA = "${state.allyKills}/${state.enemyKills}/5",
                finalGoldDiff = state.teamGoldDiff,
                recommendationsCount = 15,
                topObjectiveContested = state.currentObjective?.displayName ?: "Caesar / Rồng",
                coachScore = if (isWin) 92 else 74,
                tacticalNotes = "Trợ Lý AI hỗ trợ cảnh báo giọng nói và gợi ý lên đồ khắc chế trực tiếp."
            )
            val matchId = matchRepository.insertMatch(match)
            val logs = listOf(
                TacticalLogEntity(
                    matchId = matchId,
                    timestampSeconds = state.matchTimeSeconds / 2,
                    eventType = "MỤC TIÊU",
                    calloutText = "Ưu tiên mục tiêu: ${state.currentObjective?.displayName ?: "Mục tiêu lớn"}",
                    priorityLevel = 2
                ),
                TacticalLogEntity(
                    matchId = matchId,
                    timestampSeconds = state.matchTimeSeconds,
                    eventType = "CẢNH BÁO",
                    calloutText = state.dangerWarning,
                    priorityLevel = 3
                )
            )
            matchRepository.insertTacticalLogs(logs)
        }
    }

    override fun onCleared() {
        voiceCoach.release()
        super.onCleared()
    }
}
