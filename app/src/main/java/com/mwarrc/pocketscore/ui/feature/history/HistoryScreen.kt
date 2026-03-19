package com.mwarrc.pocketscore.ui.feature.history

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.ui.feature.history.components.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import android.os.Build
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.animation.AnimatedVisibility

/**
 * Main history screen displaying past games and player statistics.
 * 
 * Features a tabbed interface with:
 * - **Matches**: Chronological list of all games with resume/archive/share actions
 * - **Settle** (conditional): Cost-splitting calculator for shared game expenses
 * - **Leaderboard**: Player rankings and statistics across all games
 * - **Friends**: Player management with rename functionality
 * 
 * The Settle tab is only shown if `matchSplitEnabled` is true in settings.
 * 
 * @param history Complete game history including past games and player list
 * @param settings App settings controlling feature visibility and behavior
 * @param onNavigateToGame Callback to return to active game or home screen
 * @param onNavigateToSettings Callback to open settings screen
 * @param onResumeGame Callback to resume a saved game (game, shouldOverrideHistory)
 * @param onDeleteGame Callback to permanently delete a game by ID
 * @param onArchiveGame Callback to toggle archive status of a game by ID
 * @param onShareGame Callback to share a game record by ID
 * @param onRename Callback to rename a player globally (oldName, newName)
 * @param onViewDetails Callback to view detailed match analysis by game ID
 * @param onUpdateSettings Callback to update app settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: GameHistory,
    settings: AppSettings,
    onNavigateToGame: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onResumeGame: (GameState, Boolean) -> Unit,
    onDeleteGame: (String) -> Unit,
    onArchiveGame: (String) -> Unit,
    onShareGame: (String) -> Unit,
    onShareMultipleGames: (Set<String>) -> Unit,
    onRename: (String, String) -> Unit,
    onViewDetails: (String) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedMatchIds by remember { mutableStateOf(setOf<String>()) }
    var selectionMode by remember { mutableStateOf(false) }

    // Clear selection when exiting selection mode
    LaunchedEffect(selectionMode) {
        if (!selectionMode) selectedMatchIds = emptySet()
    }

    // Define the tab structure clearly
    data class HistoryTabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val content: @Composable () -> Unit)

    val activeTabs = remember(settings, history) {
        val list = mutableListOf<HistoryTabItem>()
        
        // Tab 0: Matches
        list.add(HistoryTabItem("Matches", Icons.Default.History) {
            GameHistoryTab(
                history = history,
                onNavigateToGame = onNavigateToGame,
                onResumeGame = onResumeGame,
                onDeleteGame = onDeleteGame,
                onArchiveGame = onArchiveGame,
                onShareGame = onShareGame,
                onViewDetails = onViewDetails,
                selectionMode = selectionMode,
                selectedIds = selectedMatchIds,
                onToggleSelection = { id ->
                    selectedMatchIds = if (id in selectedMatchIds) {
                        selectedMatchIds - id
                    } else {
                        selectedMatchIds + id
                    }
                    if (selectedMatchIds.isEmpty()) selectionMode = false
                },
                onEnterSelectionMode = { id ->
                    selectionMode = true
                    selectedMatchIds = setOf(id)
                }
            )
        })

        // Tab 1: Settle (Conditional)
        if (settings.matchSplitEnabled) {
            list.add(HistoryTabItem("Settle", Icons.Default.Payments) {
                MatchSplitTab(
                    history = history,
                    settings = settings,
                    selectedMatchIds = selectedMatchIds,
                    onSelectMatches = { selectedMatchIds = it },
                    onUpdateSettings = onUpdateSettings
                )
            })
        }

        // Tab 2: Leaderboard
        list.add(HistoryTabItem("Ranks", Icons.Default.Leaderboard) {
            LeaderboardTab(
                history = history,
                settings = settings
            )
        })

        // Tab 3: Players
        list.add(HistoryTabItem("Players", Icons.Default.Group) {
            FriendsTab(
                settings = settings,
                history = history,
                onUpdateSettings = onUpdateSettings,
                onRename = onRename
            )
        })
        
        list
    }

    val pagerState = rememberPagerState(pageCount = { activeTabs.size })

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (selectionMode) "${selectedMatchIds.size} Selected" else "Records",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    if (selectionMode) {
                        IconButton(onClick = { selectionMode = false }) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    } else {
                        IconButton(onClick = onNavigateToGame) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    if (!selectionMode) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.safeDrawing // Ensures it avoids top cutouts/notch
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()), // Use only top padding from Scaffold
                beyondViewportPageCount = 1
            ) { page ->
                activeTabs[page].content()
            }

            // Floating Navigation Bars
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Handles actual system nav bar if shown
                    .padding(bottom = 8.dp) // Tighter clearance from screen edge
            ) {
                MatchSelectionBar(
                    selectedCount = selectedMatchIds.size,
                    onExport = { 
                        onShareMultipleGames(selectedMatchIds)
                        selectionMode = false
                    },
                    isVisible = selectionMode
                )

                FloatingHistoryNavBar(
                    tabs = activeTabs.map { HistoryTab(it.title, it.icon) },
                    selectedIndex = pagerState.currentPage,
                    onTabClick = { index -> 
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    isVisible = !selectionMode
                )
            }
        }
    }
}
