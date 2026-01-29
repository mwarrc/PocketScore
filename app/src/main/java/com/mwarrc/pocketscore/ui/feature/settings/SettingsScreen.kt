package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsDivider
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import com.mwarrc.pocketscore.ui.feature.settings.components.StrictModeInfoDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    var strictModeTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var showStrictModeInfo by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToGame) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // GAMEPLAY SETTINGS
            Text(
                "Gameplay",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            SettingsItem(
                title = "Haptic Feedback",
                subtitle = "Subtle vibration on score changes",
                icon = Icons.Default.TouchApp,
                trailing = {
                    Switch(
                        checked = settings.hapticFeedbackEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(hapticFeedbackEnabled = enabled) }
                        }
                    )
                }
            )

            SettingsItem(
                title = "Leader Spotlight",
                subtitle = "Highlight player with highest points",
                icon = Icons.Default.Palette,
                trailing = {
                    Switch(
                        checked = settings.leaderSpotlightEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(leaderSpotlightEnabled = enabled) }
                        }
                    )
                }
            )

            SettingsItem(
                title = "Strict Turn Mode",
                subtitle = if (settings.strictTurnMode) "Security Active" else "Quick play enabled",
                icon = if (settings.strictTurnMode) Icons.Default.Lock else Icons.Default.LockOpen,
                onIconClick = { showStrictModeInfo = true },
                modifier = Modifier.clickable {
                    if (!settings.strictTurnMode) {
                        onUpdateSettings { it.copy(strictTurnMode = true) }
                    } else {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 500) {
                            strictModeTapCount++
                        } else {
                            strictModeTapCount = 1
                        }
                        lastTapTime = now

                        if (strictModeTapCount >= 3) {
                            onUpdateSettings { it.copy(strictTurnMode = false) }
                            strictModeTapCount = 0
                        }
                    }
                },
                trailing = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (settings.strictTurnMode) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (settings.strictTurnMode) "ON" else "OFF",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = if (settings.strictTurnMode) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            )

            if (showStrictModeInfo) {
                StrictModeInfoDialog(onDismissRequest = { showStrictModeInfo = false })
            }

            SettingsItem(
                title = "Upcoming Features",
                subtitle = "Toggle 'Coming Soon' visibility in setup",
                icon = Icons.Default.Extension,
                trailing = {
                    Switch(
                        checked = settings.showComingSoonFeatures,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(showComingSoonFeatures = enabled) }
                        }
                    )
                }
            )

            SettingsDivider(alpha = 0.5f)

            // APPEARANCE SETTINGS
            Text(
                "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Switch between Light and Dark",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf("System", "Light", "Dark")
                    val selectedIndex = when (settings.isDarkMode) {
                        null -> 0
                        false -> 1
                        true -> 2
                    }
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                val newVal = when (index) {
                                    0 -> null
                                    1 -> false
                                    2 -> true
                                    else -> null
                                }
                                onUpdateSettings { it.copy(isDarkMode = newVal) }
                            },
                            selected = index == selectedIndex,
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            SettingsItem(
                title = "Default Layout",
                subtitle = "Preferred scoreboard view",
                icon = Icons.Default.ViewModule,
                trailing = {
                    val isGrid = settings.defaultLayout == ScoreboardLayout.GRID
                    TextButton(
                        onClick = {
                            onUpdateSettings {
                                it.copy(
                                    defaultLayout = if (isGrid) {
                                        ScoreboardLayout.LIST
                                    } else {
                                        ScoreboardLayout.GRID
                                    }
                                )
                            }
                        }
                    ) {
                        Text(if (isGrid) "Grid" else "List")
                    }
                }
            )

            SettingsDivider(alpha = 0.5f)

            // LIMITS
            Text(
                "Limits",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Max Players", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Allowed in setup: ${settings.maxPlayers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Slider(
                    value = settings.maxPlayers.toFloat(),
                    onValueChange = { value ->
                        onUpdateSettings { it.copy(maxPlayers = value.toInt()) }
                    },
                    valueRange = 8f..100f, /* to be replace with a better input method */
                    steps = 24,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            // ABOUT LINK - SEPARATED
            Surface(
                onClick = onNavigateToAbout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "About PocketScore",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mission, Version and Info",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                "v0.1.0-expressive",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

