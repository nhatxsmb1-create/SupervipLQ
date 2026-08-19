package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.SupportedGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "arena_coach_settings")

data class CoachSettings(
    val voiceEnabled: Boolean = true,
    val voicePitch: Float = 1.0f,
    val voiceSpeechRate: Float = 1.15f, // Slightly brisk for rapid tactical gaming callouts
    val voicePriorityOnly: Boolean = false,
    val overlayTransparency: Float = 0.90f,
    val overlayScale: Float = 1.0f,
    val overlayDefaultExpanded: Boolean = true,
    val captureIntervalIdleSeconds: Int = 5,
    val captureIntervalCombatSeconds: Int = 1,
    val selectedGame: SupportedGame = SupportedGame.ARENA_OF_VALOR,
    val simulationModeEnabled: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val VOICE_SPEECH_RATE = floatPreferencesKey("voice_speech_rate")
        val VOICE_PRIORITY_ONLY = booleanPreferencesKey("voice_priority_only")
        val OVERLAY_TRANSPARENCY = floatPreferencesKey("overlay_transparency")
        val OVERLAY_SCALE = floatPreferencesKey("overlay_scale")
        val OVERLAY_EXPANDED = booleanPreferencesKey("overlay_expanded")
        val CAPTURE_IDLE_SEC = intPreferencesKey("capture_idle_sec")
        val CAPTURE_COMBAT_SEC = intPreferencesKey("capture_combat_sec")
        val SELECTED_GAME = stringPreferencesKey("selected_game")
        val SIMULATION_MODE = booleanPreferencesKey("simulation_mode")
    }

    val coachSettingsFlow: Flow<CoachSettings> = context.dataStore.data.map { preferences ->
        val voiceEnabled = preferences[PreferencesKeys.VOICE_ENABLED] ?: true
        val voicePitch = preferences[PreferencesKeys.VOICE_PITCH] ?: 1.0f
        val voiceSpeechRate = preferences[PreferencesKeys.VOICE_SPEECH_RATE] ?: 1.15f
        val voicePriorityOnly = preferences[PreferencesKeys.VOICE_PRIORITY_ONLY] ?: false
        val overlayTransparency = preferences[PreferencesKeys.OVERLAY_TRANSPARENCY] ?: 0.90f
        val overlayScale = preferences[PreferencesKeys.OVERLAY_SCALE] ?: 1.0f
        val overlayExpanded = preferences[PreferencesKeys.OVERLAY_EXPANDED] ?: true
        val captureIdleSec = preferences[PreferencesKeys.CAPTURE_IDLE_SEC] ?: 5
        val captureCombatSec = preferences[PreferencesKeys.CAPTURE_COMBAT_SEC] ?: 1
        val selectedGameName = preferences[PreferencesKeys.SELECTED_GAME] ?: SupportedGame.ARENA_OF_VALOR.name
        val simulationMode = preferences[PreferencesKeys.SIMULATION_MODE] ?: false

        val game = try {
            SupportedGame.valueOf(selectedGameName)
        } catch (_: Exception) {
            SupportedGame.ARENA_OF_VALOR
        }

        CoachSettings(
            voiceEnabled = voiceEnabled,
            voicePitch = voicePitch,
            voiceSpeechRate = voiceSpeechRate,
            voicePriorityOnly = voicePriorityOnly,
            overlayTransparency = overlayTransparency,
            overlayScale = overlayScale,
            overlayDefaultExpanded = overlayExpanded,
            captureIntervalIdleSeconds = captureIdleSec,
            captureIntervalCombatSeconds = captureCombatSec,
            selectedGame = game,
            simulationModeEnabled = simulationMode
        )
    }

    suspend fun updateVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VOICE_ENABLED] = enabled }
    }

    suspend fun updateVoicePitch(pitch: Float) {
        context.dataStore.edit { it[PreferencesKeys.VOICE_PITCH] = pitch }
    }

    suspend fun updateVoiceSpeechRate(rate: Float) {
        context.dataStore.edit { it[PreferencesKeys.VOICE_SPEECH_RATE] = rate }
    }

    suspend fun updateVoicePriorityOnly(priorityOnly: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VOICE_PRIORITY_ONLY] = priorityOnly }
    }

    suspend fun updateOverlayTransparency(transparency: Float) {
        context.dataStore.edit { it[PreferencesKeys.OVERLAY_TRANSPARENCY] = transparency }
    }

    suspend fun updateOverlayScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.OVERLAY_SCALE] = scale }
    }

    suspend fun updateOverlayExpanded(expanded: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.OVERLAY_EXPANDED] = expanded }
    }

    suspend fun updateSelectedGame(game: SupportedGame) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_GAME] = game.name }
    }

    suspend fun updateSimulationMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SIMULATION_MODE] = enabled }
    }
}
