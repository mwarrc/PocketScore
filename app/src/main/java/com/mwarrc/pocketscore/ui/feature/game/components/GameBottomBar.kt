package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings

/**
 * The primary bottom navigation bar for the game screen.
 * 
 * Provides access to history, calculator, ball tracking, 
 * settings, and player management. Styled as a floating pill.
 * 
 * @param settings Current application settings
 * @param showHistoryBadge Whether to show a notification dot on History
 * @param onShowHelp Callback for help overlay
 * @param onShowHistory Callback for match history
 * @param onShowCalculator Callback for expression calculator
 * @param onShowPoolProbability Callback for contender math overlay
 * @param onShowQuickSettings Callback for settings sheet
 * @param onShowManagePlayers Callback for roster management
 */
@Composable
fun GameBottomBar(
    settings: AppSettings,
    showHistoryBadge: Boolean,
    onShowHelp: () -> Unit,
    onShowHistory: () -> Unit,
    onShowCalculator: () -> Unit,
    onShowPoolProbability: () -> Unit,
    onShowQuickSettings: () -> Unit,
    onShowManagePlayers: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (settings.showHelpInNavBar) {
                BottomNavItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = "Help",
                    onClick = onShowHelp
                )
            }
            BottomNavItem(
                icon = Icons.Default.History,
                label = "History",
                hasBadge = showHistoryBadge,
                onClick = onShowHistory
            )
            BottomNavItem(
                icon = Icons.Default.Calculate,
                label = "Calc",
                onClick = onShowCalculator
            )
            if (settings.poolBallManagementEnabled) {
                BottomNavItem(
                    icon = Icons.Default.Analytics,
                    label = "Balls",
                    onClick = onShowPoolProbability
                )
            }

            BottomNavItem(
                icon = Icons.Outlined.Tune,
                label = "Settings",
                onClick = onShowQuickSettings
            )
            BottomNavItem(
                icon = Icons.Outlined.Group,
                label = "Players",
                onClick = onShowManagePlayers
            )
        }
    }
}
