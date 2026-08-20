package com.example.debug

import com.example.model.GameState

data class DebugState(
    val isEnabled: Boolean = false,
    val gameState: GameState = GameState(),
    val fps: Float = 0f,
    val lastCaptureTimestamp: Long = System.currentTimeMillis()
)
