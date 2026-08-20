package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Trợ Lý Live", Icons.Default.Dashboard)
    data object History : Screen("history", "Lịch Sử Đấu", Icons.Default.Analytics)
    data object Strategy : Screen("strategy", "Khắc Chế", Icons.AutoMirrored.Filled.MenuBook)
    data object Settings : Screen("settings", "Cài Đặt", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.History,
    Screen.Strategy,
    Screen.Settings
)
