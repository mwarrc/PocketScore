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
    onShareGame: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onViewDetails: (String) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val navigationState = remember(settings.matchSplitEnabled) {
        val titles = mutableListOf("Matches", "Leaderboard", "Pool")
        val icons = mutableListOf(Icons.Default.History, Icons.Default.Leaderboard, Icons.Default.Group)
        
        if (settings.matchSplitEnabled) {
            titles.add(1, "Settle")
            icons.add(1, Icons.Default.Payments)
        }
        titles to icons
    }
    val (titles, icons) = navigationState

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
                    windowInsets = WindowInsets(top = 0.dp) // Handled by statusBarsPadding below if needed, or just let scaffold handle it
                )
                
                // Elegant Pill-Style Tabs
                val indicator = @Composable { tabPositions: List<TabPosition> ->
                    if (selectedTabIndex < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(54.dp) // More spacious height
                                .padding(horizontal = 4.dp, vertical = 8.dp) // Nicer pill floating effect
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .zIndex(-1f) // Place behind content
                        )
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = indicator
                ) {
                    titles.forEachIndexed { index, title ->
                        val selected = selectedTabIndex == index
                        Tab(
                            selected = selected,
                            onClick = { selectedTabIndex = index },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        icons[index],
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val contentTab = if (settings.matchSplitEnabled) {
                val currentTitle = titles[selectedTabIndex]
                when (currentTitle) {
                    "Matches" -> 0
                    "Settle" -> 3
                    "Leaderboard" -> 1
                    "Pool" -> 2
                    else -> 0
                }
            } else {
                selectedTabIndex
            }

            when (contentTab) {
                0 -> GameHistoryTab(
                    history = history,
                    onNavigateToGame = onNavigateToGame,
                    onResumeGame = onResumeGame,
                    onDeleteGame = onDeleteGame,
                    onShareGame = onShareGame,
                    onViewDetails = onViewDetails
                )
                1 -> LeaderboardTab(
                    history = history,
                    settings = settings
                )
                2 -> FriendsTab(
                    settings = settings,
                    history = history,
                    onUpdateSettings = onUpdateSettings,
                    onRename = onRename
                )
                3 -> MatchSplitTab(
                    history = history,
                    settings = settings,
                    onUpdateSettings = onUpdateSettings
                )
            }
        }
    }
}
