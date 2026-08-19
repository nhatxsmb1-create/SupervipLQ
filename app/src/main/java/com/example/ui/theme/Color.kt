package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Esports Obsidian Midnight Theme
val EsportsDarkBg = Color(0xFF060A14)
val EsportsSurface = Color(0xFF0D1424)
val EsportsSurfaceElevated = Color(0xFF141F36)
val EsportsSurfaceHighlight = Color(0xFF1C2C4C)
val EsportsGlassBg = Color(0xCC0D162A)

// Imperial Gold Accents (Arena of Valor Champion Gold)
val ImperialGold = Color(0xFFFFC837)
val ImperialGoldLight = Color(0xFFFFE082)
val ImperialGoldDark = Color(0xFFC79200)
val RadiantAmber = Color(0xFFFF9800)

// Arcane Cyber Cyan (Energy & Mana)
val ArcaneCyan = Color(0xFF00F0FF)
val ArcaneCyanGlow = Color(0xFF38BDF8)
val ArcaneBlue = Color(0xFF2563EB)
val ArcaneIndigo = Color(0xFF6366F1)

// Esports Status Colors
val VictoryGreen = Color(0xFF00E676)
val DefeatRed = Color(0xFFFF2A4D)
val AlertOrange = Color(0xFFFF7A00)
val ManaPurple = Color(0xFFA855F7)

// Text Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextGold = Color(0xFFFFE57F)

// Borders & Dividers
val GoldBorder = Color(0x66FFC837)
val CyanBorder = Color(0x6600F0FF)
val GlassBorder = Color(0x3338BDF8)
val DarkCardBorder = Color(0x331E293B)

// Signature Gradients
val GoldGlowGradient = Brush.horizontalGradient(
    listOf(ImperialGold, RadiantAmber, ImperialGoldLight)
)

val CyanEnergyGradient = Brush.horizontalGradient(
    listOf(ArcaneCyan, Color(0xFF0088FF), ArcaneIndigo)
)

val EsportsCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF131D33), Color(0xFF0A101F))
)

val DangerGlowGradient = Brush.horizontalGradient(
    listOf(DefeatRed, AlertOrange)
)

val VictoryGlowGradient = Brush.horizontalGradient(
    listOf(VictoryGreen, Color(0xFF00B0FF))
)

// Aliases for compatibility
val CyberCyan = ArcaneCyan
val TacticalGold = ImperialGold
val AlertRed = DefeatRed
val SuccessGreen = VictoryGreen
val DarkBg = EsportsDarkBg
val DarkSurface = EsportsSurface
val DarkSurfaceVariant = EsportsSurfaceElevated
val ArcanePurple = ManaPurple
