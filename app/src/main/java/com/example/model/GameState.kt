package com.example.model

import com.example.detection.DetectedUIComponent
import java.util.Locale

data class HeroScoreboardEntry(
    val heroName: String,
    val playerName: String = "",
    val kda: String = "",
    val gold: Int = 0,
    val level: Int = 1,
    val isAlly: Boolean = true,
    val items: List<String> = emptyList()
)

data class GameState(
    val matchActive: Boolean? = null,
    val screenState: ScreenState = ScreenState.OUTSIDE_GAME,
    val matchTimeSeconds: Int? = null,
    val allyKills: Int? = null,
    val enemyKills: Int? = null,
    val goldDifference: Int? = null,
    val playerHero: String? = null,
    val currentShopGold: Int? = null,
    val allyTotalGold: Int? = null,
    val enemyTotalGold: Int? = null,
    val allyTowers: Int? = null,
    val enemyTowers: Int? = null,
    val allyDragons: Int? = null,
    val enemyDragons: Int? = null,
    val allySlayers: Int? = null,
    val enemySlayers: Int? = null,
    val allyHeroes: List<String>? = null,
    val enemyHeroes: List<String>? = null,
    val allyRoster: List<HeroScoreboardEntry> = emptyList(),
    val enemyRoster: List<HeroScoreboardEntry> = emptyList(),
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
    val rawOcrSummary: String = "",
    val detailedRosterAnalysis: String? = null,
    val counterBuildAdvice: List<String> = emptyList()
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

