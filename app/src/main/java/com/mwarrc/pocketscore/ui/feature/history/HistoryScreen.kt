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
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Payments

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
    onRename: (String, String) -> Unit,
    onViewDetails: (String) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    val scope = rememberCoroutineScope()

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
                onViewDetails = onViewDetails
            )
        })

        // Tab 1: Settle (Conditional)
        if (settings.matchSplitEnabled) {
            list.add(HistoryTabItem("Settle", Icons.Default.Payments) {
                MatchSplitTab(
                    history = history,
                    settings = settings,
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
                            "Records",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToGame) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
