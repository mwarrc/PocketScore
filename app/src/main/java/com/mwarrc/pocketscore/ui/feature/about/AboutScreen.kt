package com.mwarrc.pocketscore.ui.feature.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.feature.about.components.AboutHeader
import com.mwarrc.pocketscore.ui.feature.about.components.AboutInfoCard
import com.mwarrc.pocketscore.ui.feature.about.components.DeveloperSocials
import com.mwarrc.pocketscore.ui.feature.about.components.AboutStatsView
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState

/**
 * Main About screen providing application information and developer contact.
 *
 * This screen follows a modular design pattern with components extracted into 
 * the `.components` sub-package for better maintainability.
 *
 * @param onNavigateBack Callback for the back navigation icon
 * @param onNavigateToFeedback Navigation to the user feedback screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    settings: AppSettings,
    history: GameHistory,
    onNavigateBack: () -> Unit,
    onNavigateToFeedback: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .displayCutoutPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PocketScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Content Wrapper
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (App Logo + Version)
                AboutHeader(
                    version = "v1.0.0 Expressive"
                )

                Spacer(Modifier.height(32.dp))

                // Information Section
                AboutInfoCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "The Experience",
                    description = "Crafted for focus. Featuring Material You dynamic coloring, tactile haptic feedback, and a seamless interface that adapts to your device's aesthetic."
                )

                Spacer(Modifier.height(12.dp))

                AboutInfoCard(
                    icon = Icons.Default.Terminal,
                    title = "Open Source & Tech",
                    description = "100% Kotlin & Jetpack Compose. PocketScore is open-source, built with Material 3 principles and type-safe DataStore for a modern, high-performance architecture."
                )

                Spacer(Modifier.height(12.dp))

                AboutInfoCard(
                    icon = Icons.Filled.Lock,
                    title = "Privacy Focused",
                    description = "Zero Data Collection, zero ads. All your game data stays on your device. Offline-first architecture ensures you can play anywhere, anytime."
                )

                Spacer(Modifier.height(12.dp))

                AboutInfoCard(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    title = "Feedback & Support",
                    description = "Have a bug report or a feature request? Let us know directly through the app.",
                    onClick = onNavigateToFeedback
                )
                
                // Flexible spacer to push socials to bottom if screen is tall
                Spacer(Modifier.weight(1f, fill = false))
                Spacer(Modifier.height(48.dp))

                // Refined Social Presence Section
                DeveloperSocials()
                
                Spacer(Modifier.height(32.dp))

                // Global App Statistics
                AboutStatsView(
                    totalGames = settings.gamesPlayedCount,
                    totalPlayers = settings.savedPlayerNames.size,
                    totalTimeSummary = remember(history.pastGames) {
                        calculateTotalPlayTime(history.pastGames)
                    }
                )
                
                Spacer(Modifier.height(24.dp))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

/**
 * Calculates a human-readable summary of total play time across all past games.
 */
private fun calculateTotalPlayTime(games: List<GameState>): String {
    if (games.isEmpty()) return "0m"
    
    val totalMs = games.sumOf { 
        val end = it.endTime ?: it.lastUpdate
        (end - it.startTime).coerceAtLeast(0L)
    }
    
    val totalMinutes = totalMs / (1000 * 60)
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    return when {
        totalDays >= 1 -> {
            val remainingHours = totalHours % 24
            if (remainingHours > 0) "${totalDays}d ${remainingHours}h" else "${totalDays} Days"
        }
        totalHours >= 1 -> "${totalHours}h ${totalMinutes % 60}m"
        else -> "${totalMinutes}m"
    }
}
