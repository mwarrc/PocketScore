package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingInfoDialog
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import com.mwarrc.pocketscore.ui.feature.settings.components.StrictModeInfoDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplaySettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onBack: () -> Unit
) {
    var showStrictModeInfo by remember { mutableStateOf(false) }
    var showEnforceDialog by remember { mutableStateOf(false) }
    var showSavedPlayersOffDialog by remember { mutableStateOf(false) }
    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Gameplay Rules",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Scoring & Turn Controls",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Lock Scoring Rules (Strict)",
                    subtitle = if (settings.enforceStrictMode) 
                        "Strict Mode is locked and cannot be disabled in-game" 
                    else 
                        "Enforces strict turn-taking and single score input per round",
                    icon = if (settings.enforceStrictMode) Icons.Default.Lock else Icons.Default.LockOpen,
                    onIconClick = { showStrictModeInfo = true },
                    trailing = {
                        Switch(
                            checked = settings.enforceStrictMode,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showEnforceDialog = true
                                } else {
                                    onUpdateSettings { it.copy(enforceStrictMode = false) }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.error,
                                checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                                checkedBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                )
            }

            item {
                Text(
                    "Feedback & Spotlights",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Haptic Feedback",
                    subtitle = "Vibration confirmation on score adjustments",
                    icon = Icons.Default.TouchApp,
                    onIconClick = {
                        infoDialogContent = InfoContent(
                            title = "Haptic Feedback",
                            description = "Provides tactile confirmation through subtle phone vibrations whenever you increment or decrement a score. This helps ensure your input was registered without looking at the screen.",
                            icon = Icons.Default.TouchApp
                        )
                    },
                    trailing = {
                        Switch(
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(hapticFeedbackEnabled = enabled) }
                            }
                        )
                    }
                )
            }

            item {
                SettingsItem(
                    title = "Player Spotlights",
                    subtitle = "Highlight leader and trailer on the scoreboard",
                    icon = Icons.Default.Palette,
                    onIconClick = {
                        infoDialogContent = InfoContent(
                            title = "Spotlights",
                            description = "Visually distinguishes the current leader and loser in the scoreboard. Perfect for keeping track of the front-runner and who is following the 'loser starts' rule at a glance.",
                            icon = Icons.Default.Palette
                        )
                    },
                    trailing = {
                        Switch(
                            checked = settings.leaderSpotlightEnabled || settings.loserSpotlightEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { 
                                    it.copy(
                                        leaderSpotlightEnabled = enabled,
                                        loserSpotlightEnabled = enabled
                                    ) 
                                }
                            }
                        )
                    }
                )
            }

            item {
                Text(
                    "Home & Roster Setup",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Saved Players Quick-Select",
                    subtitle = "Show quick player chips on Home Screen",
                    icon = Icons.Default.Group,
                    onIconClick = {
                        infoDialogContent = InfoContent(
                            title = "Saved Players",
                            description = "Enables a quick-select roster on the home screen featuring your most frequent players. This makes setting up a new match much faster by avoiding manual name entry.",
                            icon = Icons.Default.Group
                        )
                    },
                    trailing = {
                        Switch(
                            checked = settings.showQuickSelectOnHome,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    showSavedPlayersOffDialog = true
                                } else {
                                    onUpdateSettings { it.copy(showQuickSelectOnHome = true) }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = if (!settings.showQuickSelectOnHome) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = if (!settings.showQuickSelectOnHome) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = if (!settings.showQuickSelectOnHome) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else Color.Transparent
                            )
                        )
                    }
                )
            }
        }
    }

    infoDialogContent?.let { info ->
        SettingInfoDialog(
            title = info.title,
            description = info.description,
            icon = info.icon,
            onDismiss = { infoDialogContent = null }
        )
    }

    if (showEnforceDialog) {
        AlertDialog(
            onDismissRequest = { showEnforceDialog = false },
            icon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Enforce Strict Mode?") },
            text = {
                Text("When this is enabled, 'Strict Turn Mode' will be forced ON and cannot be turned off during a game session. This feature enforces single score inputs per player's round. Not recommended for casual games.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings { 
                            it.copy(
                                enforceStrictMode = true, 
                                strictTurnMode = true, 
                                autoNextTurn = true,
                                autoAdvanceOnNegativeOnly = false
                            ) 
                        }
                        showEnforceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Confirm Lock") }
            },
            dismissButton = {
                TextButton(onClick = { showEnforceDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSavedPlayersOffDialog) {
        AlertDialog(
            onDismissRequest = { showSavedPlayersOffDialog = false },
            icon = { Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hide Saved Players?") },
            text = {
                Text("This will remove the quick-select roster pool from your Home Screen. You'll need to manually type each player's name to start a match.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings { it.copy(showQuickSelectOnHome = false) }
                        showSavedPlayersOffDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Disable Roster") }
            },
            dismissButton = {
                TextButton(onClick = { showSavedPlayersOffDialog = false }) { Text("Keep Enabled") }
            }
        )
    }

    if (showStrictModeInfo) {
        StrictModeInfoDialog(onDismissRequest = { showStrictModeInfo = false })
    }
}
