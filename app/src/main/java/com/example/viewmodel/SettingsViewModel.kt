package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ArenaCoachApp
import com.example.data.repository.CoachSettings
import com.example.model.SupportedGame
import com.example.voice.VoiceCoach
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = (application as ArenaCoachApp).userPreferencesRepository
    private val voiceCoach = VoiceCoach(application)

    val settings: StateFlow<CoachSettings> = preferencesRepository.coachSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CoachSettings()
    )

    val voiceEnabled: StateFlow<Boolean> = settings.map { it.voiceEnabled }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val highPriorityOnly: StateFlow<Boolean> = settings.map { it.voicePriorityOnly }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val voiceSpeed: StateFlow<Float> = settings.map { it.voiceSpeechRate }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.15f
    )

    val voicePitch: StateFlow<Float> = settings.map { it.voicePitch }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.0f
    )

    val overlayAlpha: StateFlow<Float> = settings.map { it.overlayTransparency }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.90f
    )

    val selectedGame: StateFlow<SupportedGame> = settings.map { it.selectedGame }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SupportedGame.ARENA_OF_VALOR
    )

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateVoiceEnabled(enabled) }
    }

    fun setVoicePitch(pitch: Float) {
        viewModelScope.launch { preferencesRepository.updateVoicePitch(pitch) }
    }

    fun setVoiceSpeechRate(rate: Float) {
        viewModelScope.launch { preferencesRepository.updateVoiceSpeechRate(rate) }
    }

    fun setVoiceSpeed(speed: Float) {
        setVoiceSpeechRate(speed)
    }

    fun setVoicePriorityOnly(priorityOnly: Boolean) {
        viewModelScope.launch { preferencesRepository.updateVoicePriorityOnly(priorityOnly) }
    }

    fun setHighPriorityOnly(highPriority: Boolean) {
        setVoicePriorityOnly(highPriority)
    }

    fun setOverlayTransparency(transparency: Float) {
        viewModelScope.launch { preferencesRepository.updateOverlayTransparency(transparency) }
    }

    fun setOverlayAlpha(alpha: Float) {
        setOverlayTransparency(alpha)
    }

    fun setOverlayScale(scale: Float) {
        viewModelScope.launch { preferencesRepository.updateOverlayScale(scale) }
    }

    fun setSelectedGame(game: SupportedGame) {
        viewModelScope.launch { preferencesRepository.updateSelectedGame(game) }
    }

    fun setSimulationMode(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateSimulationMode(enabled) }
    }

    fun testVoiceCallout(phrase: String = "Kẻ địch biến mất ở Sông") {
        val current = settings.value
        voiceCoach.setVoiceParams(current.voicePitch, current.voiceSpeechRate)
        voiceCoach.speakCallout(phrase, "test_callout", 3, forceIgnoreCooldown = true)
    }

    override fun onCleared() {
        voiceCoach.release()
        super.onCleared()
    }
}
