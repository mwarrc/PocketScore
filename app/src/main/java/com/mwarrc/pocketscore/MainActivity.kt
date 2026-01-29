package com.mwarrc.pocketscore

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mwarrc.pocketscore.data.repository.GameRepositoryImpl
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.about.AboutScreen
import com.mwarrc.pocketscore.ui.comingsoon.ComingSoonScreen
import com.mwarrc.pocketscore.ui.feature.game.GameScreen
import com.mwarrc.pocketscore.ui.feature.history.HistoryScreen
import com.mwarrc.pocketscore.ui.feature.settings.SettingsScreen
import com.mwarrc.pocketscore.ui.feature.setup.SetupScreen
import com.mwarrc.pocketscore.ui.feature.splash.SplashScreen
import com.mwarrc.pocketscore.ui.feature.onboarding.OnboardingScreen
import com.mwarrc.pocketscore.ui.theme.PocketScoreTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mwarrc.pocketscore.ui.viewmodel.GameViewModel
import com.mwarrc.pocketscore.ui.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        
        setContent {
            val repository = GameRepositoryImpl(applicationContext)
            val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository))
            val appState by viewModel.state.collectAsState()
            val navController = rememberNavController()
            
            var showSplash by remember { mutableStateOf(true) }
            var showOnboarding by remember { mutableStateOf(false) }
            
            // Determine initial destination after splash
            val startDestination = when {
                appState.gameState.isGameActive -> "game"
                else -> "setup"
            }

            // Handle WakeLock based on game state
            LaunchedEffect(appState.gameState.isGameActive) {
                if (appState.gameState.isGameActive) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }


            val darkTheme = when (appState.settings.isDarkMode) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            PocketScoreTheme(darkTheme = darkTheme) {
                when {
                    showSplash -> {
                        SplashScreen(
                            onTimeout = {
                                showSplash = false
                                if (!appState.settings.hasSeenOnboarding) {
                                    showOnboarding = true
                                }
                            }
                        )
                    }
                    showOnboarding -> {
                        OnboardingScreen(
                            onComplete = {
                                showOnboarding = false
                                viewModel.updateSettings { it.copy(hasSeenOnboarding = true) }
                            }
                        )
                    }
                    else -> {
                        // Sync navigation with game activity state - ONLY when NavHost is present
                        LaunchedEffect(appState.gameState.isGameActive) {
                            if (appState.gameState.isGameActive) {
                                navController.navigate("game") {
                                    popUpTo("setup") { inclusive = true }
                                }
                            } else if (navController.currentDestination?.route == "game") {
                                navController.navigate("setup") {
                                    popUpTo("game") { inclusive = true }
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("setup") {
                                SetupScreen(
                                    onStartGame = { players -> viewModel.startNewGame(players) },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToRoadmap = { navController.navigate("coming_soon") },
                                    onNavigateToAbout = { navController.navigate("about") },
                                    maxPlayers = appState.settings.maxPlayers,
                                    showComingSoonFeatures = appState.settings.showComingSoonFeatures
                                )
                            }
                            composable("game") {
                                GameScreen(
                                    players = appState.gameState.players,
                                    currentPlayerId = appState.gameState.currentPlayerId,
                                    settings = appState.settings,
                                    globalEvents = appState.gameState.globalEvents,
                                    canUndo = appState.gameState.canUndo,
                                    onUpdateScore = { id, pts -> viewModel.updateScore(id, pts) },
                                    onGlobalUndo = { viewModel.undoLastGlobalAction() },
                                    onReset = { viewModel.resetGame() },
                                    onToggleLayout = {
                                        viewModel.updateSettings { 
                                            it.copy(defaultLayout = if (it.defaultLayout == ScoreboardLayout.LIST) ScoreboardLayout.GRID else ScoreboardLayout.LIST)
                                        }
                                    },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onTogglePlayerActive = { id, active -> viewModel.setPlayerActive(id, active) },
                                    onSetCurrentPlayer = { id -> viewModel.setCurrentPlayer(id) },
                                    onNextTurn = { viewModel.nextTurn() },
                                    onUpdateSettings = { update -> viewModel.updateSettings(update) }
                                )
                            }
                            composable("history") {
                                HistoryScreen(
                                    history = appState.gameHistory,
                                    onNavigateToGame = { 
                                        if (appState.gameState.isGameActive) navController.navigate("game") else navController.navigate("setup")
                                    },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onResumeGame = { game, override -> viewModel.resumeGame(game, override) }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    settings = appState.settings,
                                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                                    onNavigateToGame = {
                                        if (appState.gameState.isGameActive) navController.navigate("game") else navController.navigate("setup")
                                    },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onNavigateToAbout = { navController.navigate("about") }
                                )
                            }
                            composable("about") {
                                AboutScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToRoadmap = { navController.navigate("coming_soon") },
                                    showComingSoonFeatures = appState.settings.showComingSoonFeatures
                                )
                            }
                            composable("coming_soon") {
                                ComingSoonScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
