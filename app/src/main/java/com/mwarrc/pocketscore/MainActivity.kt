package com.mwarrc.pocketscore

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import androidx.core.content.IntentCompat
import com.mwarrc.pocketscore.util.ShareUtils
import com.mwarrc.pocketscore.ui.feature.history.ImportPreviewScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.mwarrc.pocketscore.ui.feature.history.MatchDetailsScreen
import com.mwarrc.pocketscore.ui.feature.settings.SettingsScreen
import com.mwarrc.pocketscore.ui.feature.settings.BackupManagementScreen
import com.mwarrc.pocketscore.ui.feature.home.HomeScreen
import com.mwarrc.pocketscore.ui.feature.roadmap.RoadmapScreen
import com.mwarrc.pocketscore.ui.feature.upcoming.UpcomingFeaturesScreen
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

        handleIntent(intent)
        
        setContent {
            val repository = remember { GameRepositoryImpl(applicationContext) }
            val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository))
            val appState by viewModel.state.collectAsState()
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            
            var showSplash by remember { mutableStateOf(true) }
            var showOnboarding by remember { mutableStateOf(false) }
            var incomingShareData by remember { mutableStateOf<PocketScoreShare?>(null) }
            var selectedMatchId by remember { mutableStateOf<String?>(null) }

            // Launcher for importing .pscore files manually
            val filePickerLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    val data = ShareUtils.decodeFromUri(this@MainActivity, it)
                    incomingShareData = data
                }
            }

            // Global listener for incoming data from parent activity
            LaunchedEffect(intent) {
                // Re-check intent if it changed
            }

            // Expose a way to update incoming data from outside the compose tree if needed
            onDataReceived = { data -> incomingShareData = data }

            // Handle data received before setContent was ready
            LaunchedEffect(Unit) {
                pendingShareData?.let {
                    incomingShareData = it
                    pendingShareData = null
                }
            }
            
            // Determine initial destination after splash
            val startDestination = when {
                appState.gameState.isGameActive -> "game"
                else -> "setup"
            }

            // Request storage permissions on launch for Ghost Backups
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Handle results if needed */ }

            fun requestStoragePermissions() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.addCategory("android.intent.category.DEFAULT")
                            intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                            startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent()
                            intent.action = android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                            startActivity(intent)
                        }
                    }
                } else {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            }

            fun hasStoragePermission(): Boolean {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
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
                                HomeScreen(
                                    settings = appState.settings,
                                    hasActiveGame = appState.gameState.isGameActive,
                                    onStartGame = { players -> viewModel.startNewGame(players) },
                                    onResumeGame = { navController.navigate("game") },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToAbout = { navController.navigate("about") },
                                    onNavigateToRoadmap = { navController.navigate("roadmap") },
                                    onNavigateToUpcoming = { navController.navigate("upcoming") },
                                    onUpdateSettings = { update -> viewModel.updateSettings(update) }
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
                                    onReset = { finalized -> viewModel.resetGame(finalized) },
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
                                    onResumeGame = { game, override -> viewModel.resumeGame(game, override) },
                                    onDeleteGame = { id -> viewModel.deleteGameFromHistory(id) },
                                    settings = appState.settings,
                                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                                    onRename = { old, new -> viewModel.renamePlayer(old, new) },
                                    onViewDetails = { id ->
                                        selectedMatchId = id
                                        navController.navigate("match_details")
                                    },
                                    onShareGame = { id -> 
                                        scope.launch {
                                            val share = viewModel.getShareData(id)
                                            shareData(share, "pocketscore_match_${id.take(8)}.pscore")
                                        }
                                    }
                                )
                            }
                            composable("match_details") {
                                val match = appState.gameHistory.pastGames.find { it.id == selectedMatchId }
                                if (match != null) {
                                    MatchDetailsScreen(
                                        game = match,
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    navController.popBackStack()
                                }
                            }
                            composable("settings") {
                                SettingsScreen(
                                    settings = appState.settings,
                                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                                    onNavigateToGame = {
                                        if (appState.gameState.isGameActive) navController.navigate("game") else navController.navigate("setup")
                                    },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onNavigateToAbout = { navController.navigate("about") },
                                    onExportBackup = {
                                        scope.launch {
                                            val share = viewModel.getShareData(null) // Full backup
                                            shareData(share)
                                        }
                                    },
                                    onImportBackup = {
                                        filePickerLauncher.launch("*/*") // Custom extensions can be tricky, but we'll filter in util
                                    },
                                    onNavigateToBackups = { 
                                        if (hasStoragePermission()) {
                                            navController.navigate("backups") 
                                        } else {
                                            requestStoragePermissions()
                                        }
                                    }
                                )
                            }
                            composable("backups") {
                                val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()

                                BackupManagementScreen(
                                    snapshots = snapshots,
                                    onBack = { navController.popBackStack() },
                                    onCreateSnapshot = { name ->
                                        viewModel.createSnapshot(name)
                                    },
                                    onRestoreSnapshot = { name ->
                                        scope.launch {
                                            viewModel.getSnapshotData(name)?.let { shareData ->
                                                incomingShareData = shareData
                                            }
                                        }
                                    },
                                    onDeleteSnapshot = { name ->
                                        viewModel.deleteSnapshot(name)
                                    },
                                    onShareSnapshot = { name ->
                                        scope.launch {
                                            viewModel.getSnapshotData(name)?.let { shareData ->
                                                val fileName = "${name.replace(" ", "_")}.pscore"
                                                this@MainActivity.shareData(shareData, fileName)
                                            }
                                        }
                                    },
                                    onTriggerCloudBackup = {
                                        viewModel.triggerCloudBackup()
                                    },
                                    onToggleLocalSnapshots = { enabled ->
                                        viewModel.updateSettings { it.copy(localSnapshotsEnabled = enabled) }
                                    },
                                    onRefresh = { viewModel.refreshSnapshots() },
                                    settings = appState.settings
                                )
                            }
                            composable("about") {
                                AboutScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToRoadmap = { navController.navigate("roadmap") },
                                    showComingSoonFeatures = appState.settings.showComingSoonFeatures,
                                    onUpdateSettings = { update -> viewModel.updateSettings(update) }
                                )
                            }
                            composable("coming_soon") {
                                ComingSoonScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("roadmap") {
                                RoadmapScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("upcoming") {
                                UpcomingFeaturesScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }

                // Overlay for incoming data preview
                incomingShareData?.let { data ->
                    ImportPreviewScreen(
                        shareData = data,
                        existingPlayers = appState.settings.savedPlayerNames,
                        onConfirm = { mappings ->
                            viewModel.importData(data, mappings)
                            incomingShareData = null
                        },
                        onCancel = { incomingShareData = null }
                    )
                }
            }
        }
    }

    private var onDataReceived: ((PocketScoreShare) -> Unit)? = null
    private var pendingShareData: PocketScoreShare? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        val data: Uri? = intent?.data ?: intent?.let { IntentCompat.getParcelableExtra(it, Intent.EXTRA_STREAM, Uri::class.java) }
        
        if ((action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND) && data != null) {
            val shareData = ShareUtils.decodeFromUri(this, data)
            if (shareData != null) {
                if (onDataReceived != null) {
                    onDataReceived?.invoke(shareData)
                } else {
                    pendingShareData = shareData
                }
            }
        }
    }

    private fun shareData(share: PocketScoreShare, fileName: String = "pocketscore_backup.pscore") {
        val intent = ShareUtils.getShareIntent(this, share, fileName)
        startActivity(intent)
    }
}
