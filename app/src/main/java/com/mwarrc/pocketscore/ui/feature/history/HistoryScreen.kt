package com.mwarrc.pocketscore.ui.feature.history

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
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
import com.mwarrc.pocketscore.ui.feature.history.components.FriendsTab
import com.mwarrc.pocketscore.ui.feature.history.components.GameHistoryTab
import com.mwarrc.pocketscore.ui.feature.history.components.LeaderboardTab
import com.mwarrc.pocketscore.ui.feature.history.components.MatchSplitTab
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

        // Tab 3: Pool
        list.add(HistoryTabItem("Pool", Icons.Default.Group) {
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
            .statusBarsPadding(),
        topBar = {
            Column {
                Spacer(Modifier.height(30.dp))
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (selectionMode) "${selectedMatchIds.size} Selected" else "Records",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
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
                    windowInsets = WindowInsets(top = 0.dp)
                )
                
                // Elegant Pill-Style Tabs
                val indicator = @Composable { tabPositions: List<TabPosition> ->
                    if (pagerState.currentPage < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(54.dp)
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .zIndex(-1f)
                        )
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = indicator
                ) {
                    activeTabs.forEachIndexed { index, tab ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = { 
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 2.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        tab.icon,
                                        null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        tab.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectionMode,
                enter = androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.shrinkVertically()
            ) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actions = {
                        Text(
                            "${selectedMatchIds.size} records ready",
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { 
                                onShareMultipleGames(selectedMatchIds)
                                selectionMode = false
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            icon = { Icon(Icons.Default.Share, null) },
                            text = { Text("Export Records") }
                        )
                    }
                )
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            beyondViewportPageCount = 1
        ) { page ->
            activeTabs[page].content()
        }
    }
}
