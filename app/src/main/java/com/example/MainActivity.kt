package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavScreens
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StrategyScreen
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.ArenaCoachTheme
import com.example.ui.theme.EsportsDarkBg
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CoachViewModel
import com.example.viewmodel.HistoryViewModel
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.StrategyViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArenaCoachTheme(darkTheme = true) {
                ArenaCoachAppRoot()
            }
        }
    }
}

@Composable
fun ArenaCoachAppRoot() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1200) // Hiển thị màn hình chào Esports đẹp mắt 1.2 giây
        showSplash = false
    }

    if (showSplash) {
        EsportsSplashScreen()
    } else {
        ArenaCoachMainContent()
    }
}

@Composable
fun EsportsSplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EsportsDarkBg)
            .testTag("esports_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(listOf(ImperialGold, ArcaneCyan, ImperialGoldLight, ImperialGold)),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = ImperialGold)
                    .background(Color(0xFF0A1224)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.esports_aov_icon_1787143633562),
                    contentDescription = "Logo",
                    modifier = Modifier.size(105.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TRỢ LÝ LIÊN QUÂN AI",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = TextGold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "BẢNG CHỈ HUY CHIẾN THUẬT ESPORTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.75.sp,
                color = ArcaneCyan
            )

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(
                color = ImperialGold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ArenaCoachMainContent() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val coachViewModel: CoachViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val strategyViewModel: StrategyViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = EsportsSurface,
                contentColor = Color.White
            ) {
                bottomNavScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                letterSpacing = 0.3.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ImperialGold,
                            selectedTextColor = ImperialGoldLight,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF162544)
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                HomeScreen(
                    viewModel = coachViewModel,
                    snackbarHostState = snackbarHostState
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel
                )
            }
            composable(Screen.Strategy.route) {
                StrategyScreen(
                    viewModel = strategyViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
