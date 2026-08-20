package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ImperialGold,
    onPrimary = Color(0xFF1E1700),
    primaryContainer = Color(0xFF3D2E00),
    onPrimaryContainer = ImperialGoldLight,
    secondary = ArcaneCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = RadiantAmber,
    onTertiary = Color(0xFF442B00),
    background = EsportsDarkBg,
    onBackground = TextPrimary,
    surface = EsportsSurface,
    onSurface = TextPrimary,
    surfaceVariant = EsportsSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = DefeatRed,
    onError = Color.White
)

@Composable
fun ArenaCoachTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = EsportsDarkBg.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = EsportsDarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
