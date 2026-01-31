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
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.ui.feature.history.components.FriendsTab
import com.mwarrc.pocketscore.ui.feature.history.components.GameHistoryTab
import com.mwarrc.pocketscore.ui.feature.history.components.LeaderboardTab
import androidx.compose.material.icons.filled.Leaderboard

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
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val titles = listOf("Matches", "Leaderboard", "Pool")
    val icons = listOf(Icons.Default.History, Icons.Default.Leaderboard, Icons.Default.Group)

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
                
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { 
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                            width = Dp.Unspecified // Fits content? No, standard full width
                        )
                    }
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    title, 
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            icon = { Icon(icons[index], null) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (selectedTabIndex) {
                0 -> GameHistoryTab(
                    history = history,
                    onNavigateToGame = onNavigateToGame,
                    onResumeGame = onResumeGame,
                    onDeleteGame = onDeleteGame,
                    onShareGame = onShareGame
                )
                1 -> LeaderboardTab(
                    history = history
                )
                2 -> FriendsTab(
                    settings = settings,
                    history = history,
                    onUpdateSettings = onUpdateSettings,
                    onRename = onRename
                )
            }
        }
    }
}
