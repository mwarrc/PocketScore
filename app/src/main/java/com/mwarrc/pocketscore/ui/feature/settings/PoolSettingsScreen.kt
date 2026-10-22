package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.WinnerScreenStyle
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingInfoDialog
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoolSettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateToBallValues: () -> Unit,
    onBack: () -> Unit
) {
    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }
    var showDisablePoolDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pool & Billiards",
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
                    "Table & Ball Tracking",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Pool Ball Management",
                    subtitle = "Track balls on table & calculate pot insights",
                    icon = Icons.Default.Analytics,
                    onIconClick = {
                        infoDialogContent = InfoContent(
                            title = "Pool Ball Management",
                            description = "Enables specialized tracking for pool/billiards games. It adds a secondary interface to the match screen where you can track which balls are remaining, calculate probabilities, and automatically adjust scores based on ball values.",
                            icon = Icons.Default.Analytics
                        )
                    },
                    trailing = {
                        Switch(
                            checked = settings.poolBallManagementEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    showDisablePoolDialog = true
                                } else {
                                    onUpdateSettings { it.copy(poolBallManagementEnabled = true) }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = if (!settings.poolBallManagementEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = if (!settings.poolBallManagementEnabled) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = if (!settings.poolBallManagementEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else Color.Transparent
                            )
                        )
                    }
                )
            }

            if (settings.poolBallManagementEnabled) {
                item {
                    SettingsItem(
                        title = "Auto-Remove Potted Balls",
                        subtitle = "Selecting a ball score automatically marks it potted",
                        icon = Icons.Default.Extension,
                        onIconClick = {
                            infoDialogContent = InfoContent(
                                title = "Auto-Remove Pool Balls",
                                description = "When scoring points matching a specific ball's value, that ball is automatically removed from the virtual table to save you a tap.",
                                icon = Icons.Default.Extension
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.autoRemovePoolBalls,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings { it.copy(autoRemovePoolBalls = enabled) }
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "Warn on Oversized Scores",
                        subtitle = "Ask before accepting a score above your highest ball value",
                        icon = Icons.Default.Warning,
                        onIconClick = {
                            infoDialogContent = InfoContent(
                                title = "Oversized Score Warning",
                                description = "Shows a confirmation when an entered score is worth more than the highest-value ball in your current point system. " +
                                    "This catches typos without limiting custom setups. Disable it to always accept scores silently.",
                                icon = Icons.Default.Warning
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.warnOnExceedingMaxBallValue,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings { it.copy(warnOnExceedingMaxBallValue = enabled) }
                                }
                            )
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = "Game Over Celebration",
                        subtitle = "Show celebratory effect on game finalization",
                        icon = Icons.Default.EmojiEvents,
                        onIconClick = {
                            infoDialogContent = InfoContent(
                                title = "End Game Celebration",
                                description = "Triggers a celebratory visual highlight when the match is finalized and a winner is crowned.",
                                icon = Icons.Default.EmojiEvents
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.endGameCelebrationEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings { it.copy(endGameCelebrationEnabled = enabled) }
                                }
                            )
                        }
                    )
                }

                // Winner screen style — only meaningful while the celebration is on.
                if (settings.endGameCelebrationEnabled) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.padding(horizontal = 6.dp))
                                    Text(
                                        "Winner Screen",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "How the final results are presented when a match ends",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    WinnerStyleRow(
                                        title = "Wavy Hero",
                                        subtitle = "Winning score up top, full standings listed below",
                                        selected = settings.winnerScreenStyle == WinnerScreenStyle.WAVY_HERO,
                                        onClick = {
                                            onUpdateSettings { it.copy(winnerScreenStyle = WinnerScreenStyle.WAVY_HERO) }
                                        }
                                    )
                                    WinnerStyleRow(
                                        title = "Classic",
                                        subtitle = "Centred trophy with an animated wave backdrop",
                                        selected = settings.winnerScreenStyle == WinnerScreenStyle.CLASSIC,
                                        onClick = {
                                            onUpdateSettings { it.copy(winnerScreenStyle = WinnerScreenStyle.CLASSIC) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Scoring & Rules Presets",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    Surface(
                        onClick = onNavigateToBallValues,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Custom Ball Values",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Configure point rules, presets & ball values",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (1..7).forEach { ballNum ->
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(getBallColor(ballNum)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                ballNum.toString(),
                                                color = if (ballNum == 1) Color.Black else Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
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

    if (showDisablePoolDialog) {
        AlertDialog(
            onDismissRequest = { showDisablePoolDialog = false },
            icon = { Icon(Icons.Default.Analytics, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Disable Pool Tracking?") },
            text = {
                Text("This will hide the remaining balls indicator, pot odds insights, and quick-pot buttons from your match view.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings { it.copy(poolBallManagementEnabled = false) }
                        showDisablePoolDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Disable") }
            },
            dismissButton = {
                TextButton(onClick = { showDisablePoolDialog = false }) { Text("Keep Enabled") }
            }
        )
    }
}

/**
 * A selectable row for one winner-screen style.
 *
 * Matches the radio-row pattern used by the onboarding scoring-system picker:
 * selection changes color only, never size, so the options stay a coherent set.
 *
 * @param title Short style name.
 * @param subtitle One-line explanation of the layout.
 * @param selected Whether this style is active.
 * @param onClick Called when the row is tapped.
 */
@Composable
private fun WinnerStyleRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val content = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorScheme.primary,
                    unselectedColor = colorScheme.outline
                )
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = content
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        content.copy(alpha = 0.75f)
                    } else {
                        colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
