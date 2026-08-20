package com.example.model

import com.example.detection.DetectedUIComponent

import java.util.Locale

data class GameState(
    val matchActive: Boolean? = null,
    val screenState: ScreenState = ScreenState.OUTSIDE_GAME,
    val matchTimeSeconds: Int? = null,
    val allyKills: Int? = null,
    val enemyKills: Int? = null,
    val goldDifference: Int? = null,
    val playerHero: String? = null,
    val allyHeroes: List<String>? = null,
    val enemyHeroes: List<String>? = null,
    val playerItems: List<String>? = null,
    val visibleEnemiesOnMinimap: Int? = null,
    val objectives: List<ObjectiveTarget>? = null,
    val scoreboardOpen: Boolean? = null,
    val shopOpen: Boolean? = null,
    val overallConfidence: Float = 0f,
    val frameProcessingTimeMs: Long = 0L,
    val captureIntervalMs: Long = 3000L,
    val frameChangedPercent: Float = 0f,
    val detectedComponents: List<DetectedUIComponent> = emptyList(),
    val rawOcrSummary: String = ""
) {
    val formattedMatchTime: String
        get() = matchTimeSeconds?.let {
            val min = it / 60
            val sec = it % 60
            String.format(Locale.US, "%02d:%02d", min, sec)
        } ?: "--:--"

    val formattedKda: String
        get() = if (allyKills != null && enemyKills != null) {
            "$allyKills / $enemyKills"
        } else {
            "-- / --"
        }

    val formattedGoldDiff: String
        get() = goldDifference?.let { diff ->
            val sign = if (diff >= 0) "+" else ""
            val kValue = diff / 1000f
            String.format(Locale.US, "%s%.1fk Vàng", sign, kValue)
        } ?: "-- Vàng"
}
