package com.mwarrc.pocketscore

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mwarrc.pocketscore.data.repository.GameRepositoryImpl
import com.mwarrc.pocketscore.domain.model.AppState
import com.mwarrc.pocketscore.domain.model.AppTheme
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.about.AboutScreen
import com.mwarrc.pocketscore.ui.feature.feedback.FeedbackScreen
import com.mwarrc.pocketscore.ui.feature.game.GameScreen
import com.mwarrc.pocketscore.ui.feature.history.HistoryScreen
import com.mwarrc.pocketscore.ui.feature.history.import_.ImportPreviewScreen
import com.mwarrc.pocketscore.ui.feature.history.MatchDetailsScreen
import com.mwarrc.pocketscore.ui.feature.home.HomeScreen
import com.mwarrc.pocketscore.ui.feature.onboarding.OnboardingScreen
import com.mwarrc.pocketscore.ui.feature.settings.BackupManagementScreen
import com.mwarrc.pocketscore.ui.feature.settings.BallValuesScreen
import com.mwarrc.pocketscore.ui.feature.settings.SettingsScreen
import com.mwarrc.pocketscore.ui.feature.splash.SplashScreen
import com.mwarrc.pocketscore.ui.theme.PocketScoreTheme
import com.mwarrc.pocketscore.ui.viewmodel.GameViewModel
import com.mwarrc.pocketscore.ui.viewmodel.GameViewModelFactory
import com.mwarrc.pocketscore.util.AnalyticsManager
import com.mwarrc.pocketscore.util.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main Activity of Slice.
 *
 * Handles app initialization, navigation setup, intent handling for shared files,
 * and global app configuration like themes and screen orientation.
 */
class MainActivity : ComponentActivity() {

    // Holds data received from Intents (e.g. opening a .pscore file)
    private val pendingShareDataFlow = MutableStateFlow<PocketScoreShare?>(null)

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setupOrientation()
        enableEdgeToEdge()
        initializeAnalytics()
        setupSystemBars()
        handleIntent(intent)

        setContent {
            MainContent()
        }
    }

    /**
     * Called when the activity receives a new Intent while already running.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * The main composable content of the application.
     * Sets up the ViewModel, Navigation Host, and handles global UI states like dialogs and overlays.
     */
    @Composable
    private fun MainContent() {
        val repository = remember { GameRepositoryImpl(applicationContext) }
        val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository))
        val appState by viewModel.state.collectAsStateWithLifecycle()
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()

        // Local state
        var showSplash by remember { mutableStateOf(true) }
        var showOnboarding by remember { mutableStateOf(false) }
        var incomingShareData by remember { mutableStateOf<PocketScoreShare?>(null) }
        var selectedMatchId by remember { mutableStateOf<String?>(null) }
        var storagePermissionGranted by remember { mutableStateOf(false) }
        var importError by remember { mutableStateOf<String?>(null) }
        var pendingBackupsNavigation by remember { mutableStateOf(false) }

        // Observe intent data from Flow
        val pendingData by pendingShareDataFlow.collectAsStateWithLifecycle()

        // File picker for manual import
        val filePickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                scope.launch(Dispatchers.IO) {
                    val data = ShareUtils.decodeFromUri(this@MainActivity, it)
                    withContext(Dispatchers.Main) {
                        if (data != null) {
                            incomingShareData = data
                            importError = null
                        } else {
                            importError = "Failed to import file. The file may be corrupted or in an unsupported format."
                        }
                    }
                }
            }
        }

        // Folder linker for persistent backups (survives app life & reboots)
        val backupsFolderLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            uri?.let { folderUri ->
                // Grant persistent access
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or 
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                try {
                    contentResolver.takePersistableUriPermission(folderUri, flags)
                    // Update settings via ViewModel
                    scope.launch {
                        viewModel.updateBackupsFolderUri(folderUri.toString())
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to take persistent permission", e)
                }
            }
        }

        // Permission request launcher
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            storagePermissionGranted = hasStoragePermission()
            if (storagePermissionGranted && pendingBackupsNavigation) {
                pendingBackupsNavigation = false
                navController.navigate("backups")
            }
        }

        // Initialize permissions and check for pending data
        LaunchedEffect(Unit) {
            storagePermissionGranted = hasStoragePermission()
        }

        // Handle data from Intent
        LaunchedEffect(pendingData) {
            pendingData?.let {
                incomingShareData = it
                pendingShareDataFlow.value = null // Clear after handling
            }
        }

        // Lifecycle observer for permission changes (e.g. coming back from Settings)
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val wasGranted = storagePermissionGranted
                    storagePermissionGranted = hasStoragePermission()
                    // Auto-navigate to backups if permission was just granted
                    if (!wasGranted && storagePermissionGranted && pendingBackupsNavigation) {
                        pendingBackupsNavigation = false
                        navController.navigate("backups")
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // Keep screen on during active game
        LaunchedEffect(appState.gameState.isGameActive) {
            if (appState.gameState.isGameActive) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        // Theme setup
        val darkTheme = when (appState.settings.appTheme) {
            AppTheme.DARK -> true
            AppTheme.LIGHT -> false
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }

        val startDestination = if (appState.gameState.isGameActive) "game" else "setup"

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
                    // Sync navigation with game state
                    LaunchedEffect(appState.gameState.isGameActive) {
                        val currentRoute = navController.currentDestination?.route
                        // Only auto-navigate when on setup or game screens to avoid interrupting other flows
                        val isOnGameFlowScreen = currentRoute == "setup" || currentRoute == "game"

                        if (isOnGameFlowScreen) {
                            when {
                                appState.gameState.isGameActive && currentRoute != "game" -> {
                                    navController.navigate("game") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                }
                                !appState.gameState.isGameActive && currentRoute == "game" -> {
                                    navController.navigate("setup") {
                                        popUpTo("game") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                    // Main Navigation Host
                    PocketScoreNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        appState = appState,
                        viewModel = viewModel,
                        storagePermissionGranted = storagePermissionGranted,
                        onUpdateSelectedMatchId = { selectedMatchId = it },
                        selectedMatchId = selectedMatchId,
                        requestPermission = { pendingBackupsNavigation = true; requestStoragePermissions(requestPermissionLauncher) },
                        onImportRecords = { filePickerLauncher.launch("application/octet-stream") },
                        onLinkBackupsFolder = { backupsFolderLauncher.launch(null) },
                        shareData = { share, name -> shareData(share, name) },
                        onDataRestored = { data -> incomingShareData = data }
                    )
                }
            }

            // Overlay for import preview
            incomingShareData?.let { data ->
                ImportPreviewScreen(
                    shareData = data,
                    existingPlayers = appState.settings.savedPlayerNames,
                    existingGames = appState.gameHistory.pastGames.map { it.id },
                    onConfirm = { mappings ->
                        viewModel.importData(data, mappings)
                        incomingShareData = null
                        importError = null
                    },
                    onCancel = {
                        incomingShareData = null
                        importError = null
                    }
                )
            }

            // Show import error if any
            importError?.let { error ->
                AlertDialog(
                    onDismissRequest = { importError = null },
                    title = { Text("Import Failed") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { importError = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

    /**
     * Hosts the navigation graph for the application.
     */
    @Composable
    private fun PocketScoreNavHost(
        navController: NavHostController,
        startDestination: String,
        appState: AppState,
        viewModel: GameViewModel,
        storagePermissionGranted: Boolean,
        onUpdateSelectedMatchId: (String) -> Unit,
        selectedMatchId: String?,
        requestPermission: () -> Unit,
        onImportRecords: () -> Unit,
        onLinkBackupsFolder: () -> Unit,
        shareData: (PocketScoreShare, String?) -> Unit,
        onDataRestored: (PocketScoreShare) -> Unit
    ) {
        val scope = rememberCoroutineScope()

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable("setup") {
                HomeScreen(
                    settings = appState.settings,
                    history = appState.gameHistory,
                    activePlayers = appState.gameState.players,
                    hasActiveGame = appState.gameState.isGameActive,
                    storagePermissionGranted = storagePermissionGranted,
                    onStartGame = { players -> viewModel.startNewGame(players) },
                    onResumeGame = { navController.navigate("game") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToAbout = { navController.navigate("about") },
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
                    onReset = { finalized, forceSave -> viewModel.resetGame(finalized, forceSave) },
                    onToggleLayout = {
                        viewModel.updateSettings {
                            it.copy(
                                defaultLayout = if (it.defaultLayout == ScoreboardLayout.LIST)
                                    ScoreboardLayout.GRID
                                else
                                    ScoreboardLayout.LIST
                            )
                        }
                    },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onTogglePlayerActive = { id, active -> viewModel.setPlayerActive(id, active) },
                    onSetCurrentPlayer = { id -> viewModel.setCurrentPlayer(id) },
                    onNextTurn = { viewModel.nextTurn() },
                    onRestart = { playerNames, forceSave ->
                        viewModel.startRestartMatch(playerNames, forceSave)
                    },
                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                    ballsOnTable = appState.gameState.ballsOnTable,
                    onUpdateBallsOnTable = { balls -> viewModel.updateBallsOnTable(balls) }
                )
            }

            composable("history") {
                HistoryScreen(
                    history = appState.gameHistory,
                    onNavigateToGame = {
                        if (appState.gameState.isGameActive) {
                            navController.navigate("game")
                        } else {
                            navController.navigate("setup")
                        }
                    },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onResumeGame = { game, override -> viewModel.resumeGame(game, override) },
                    onDeleteGame = { id -> viewModel.deleteGameFromHistory(id) },
                    onArchiveGame = { id -> viewModel.toggleArchiveGame(id) },
                    settings = appState.settings,
                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                    onRename = { old, new -> viewModel.renamePlayer(old, new) },
                    onViewDetails = { id ->
                        onUpdateSelectedMatchId(id)
                        navController.navigate("match_details")
                    },
                    onShareGame = { id ->
                        scope.launch {
                            val share = viewModel.getShareData(listOf(id))
                            val safeId = id.take(minOf(8, id.length))
                            shareData(share, "pocketscore_match_${safeId}.pscore")
                        }
                    },
                    onShareMultipleGames = { ids ->
                        scope.launch {
                            val share = viewModel.getShareData(ids.toList())
                            shareData(share, "pocketscore_batch_export.pscore")
                        }
                    }
                )
            }

            composable("match_details") {
                // Ensure we have a valid match ID before attempting to display
                if (selectedMatchId != null) {
                    val match = appState.gameHistory.pastGames.find { it.id == selectedMatchId }
                    if (match != null) {
                        MatchDetailsScreen(
                            game = match,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        // Match not found, navigate back
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                } else {
                    // No match ID, navigate back immediately
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable("settings") {
                SettingsScreen(
                    settings = appState.settings,
                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                    onNavigateToGame = {
                        if (appState.gameState.isGameActive) {
                            navController.navigate("game")
                        } else {
                            navController.navigate("setup")
                        }
                    },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToAbout = { navController.navigate("about") },
                    onNavigateToBackups = {
                        navController.navigate("backups")
                    },
                    onNavigateToFeedback = { navController.navigate("feedback") },
                    onNavigateToBallValues = { navController.navigate("ball_values") },
                    onLinkBackupsFolder = { onLinkBackupsFolder() },
                    onExportRecords = {
                        scope.launch {
                            val share = viewModel.getShareData(null)
                            shareData(share, "pocketscore_full_export.pscore")
                        }
                    },
                    onImportRecords = {
                        onImportRecords()
                    }
                )
            }

            composable("feedback") {
                FeedbackScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("backups") {
                val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()

                BackupManagementScreen(
                    onBack = { navController.popBackStack() },
                    snapshots = snapshots,
                    onRestore = { name ->
                        viewModel.restoreFromSnapshot(name)
                        navController.popBackStack()
                    },
                    onDelete = { viewModel.deleteSnapshot(it) },
                    onShare = { name ->
                        scope.launch {
                            try {
                                val dataToShare = viewModel.getSnapshotContent(name)
                                if (dataToShare != null) {
                                    shareData(dataToShare, "$name.pscore")
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to share snapshot", e)
                            }
                        }
                    },
                    onCreateManualSnapshot = { name -> viewModel.createLocalSnapshot(name) },
                    onLinkBackupsFolder = onLinkBackupsFolder,
                    backupsFolderUri = appState.settings.backupsFolderUri,
                    onTriggerCloudBackup = { viewModel.triggerCloudBackup() },
                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                    onRefresh = { viewModel.refreshSnapshots() },
                    settings = appState.settings
                )
            }

            composable("about") {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFeedback = { navController.navigate("feedback") }
                )
            }

            composable("ball_values") {
                BallValuesScreen(
                    settings = appState.settings,
                    onUpdateSettings = { update -> viewModel.updateSettings(update) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    /**
     * Sets proper strict portrait orientation.
     */
    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * Initializes analytics service.
     */
    private fun initializeAnalytics() {
        AnalyticsManager.initialize(this)
    }

    /**
     * Configures system bars (status/nav) to be transparent and controlled by the app.
     */
    private fun setupSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }

    /**
     * Processes incoming intents (like opening a file).
     */
    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        val data: Uri? = intent?.data ?: intent?.let {
            IntentCompat.getParcelableExtra(it, Intent.EXTRA_STREAM, Uri::class.java)
        }

        if ((action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND) && data != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val shareData = ShareUtils.decodeFromUri(this@MainActivity, data)
                if (shareData != null) {
                    pendingShareDataFlow.update { shareData }
                }
            }
        }
    }

    /**
     * Checks if storage permission is granted.
     * On Android 10+ (API 29+), we use Scoped Storage, so we don't need 
     * broad storage permissions for app-specific or shared-via-SAF operations.
     */
    private fun hasStoragePermission(): Boolean {
        // Scoped Storage means we always have "permission" to our own files and shared files
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true 
        } else {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Requests necessary storage permissions based on Android version.
     * Only needed for legacy devices (Android 9 and below).
     */
    private fun requestStoragePermissions(
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            launcher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    /**
     * Initiates the flow to share data via system chooser.
     * Uses IO dispatcher for file operations.
     */
    private fun shareData(share: PocketScoreShare, fileName: String? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            val intent = ShareUtils.getShareIntent(this@MainActivity, share, fileName)
            withContext(Dispatchers.Main) {
                startActivity(intent)
            }
        }
    }
}