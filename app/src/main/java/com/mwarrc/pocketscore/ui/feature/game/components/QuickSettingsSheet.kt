package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.AppTheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.util.ImmersiveMode
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

/**
 * A bottom sheet for toggling common game-related settings mid-match.
 * 
 * Provides quick access to themes, scoreboard layout (Grid/List), 
 * spotlight visibility, strict mode, and pool management features.
 * 
 * @param settings Current application configuration
 * @param onUpdateSettings Callback to modify one or more settings fields
 * @param onDismiss Callback to close the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsSheet(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // --- Height calculation ---
    // In immersive mode, WindowInsets.statusBars reports 0 (system bars are hidden).
    // LocalConfiguration.screenHeightDp can also exclude system bar areas on some devices.
    // The only RELIABLE source for the true physical screen height is DisplayMetrics.heightPixels.
    val density = LocalDensity.current
    val context = LocalContext.current
    val trueScreenHeightDp = with(density) {
        context.resources.displayMetrics.heightPixels.toDp()
    }
    // We want ~64dp of game screen visible above the sheet top edge.
    val maxContentHeight = trueScreenHeightDp - 64.dp

    // --- Drag handle offset ---
    // statusBars inset is 0 in immersive mode, but displayCutout inset still
    // correctly describes the notch/punch-hole area even when bars are hidden.
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    // Use whichever is larger so we always clear the camera hardware
    val topInset = maxOf(statusBarTop, cutoutTop)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // NOTE: Do NOT apply heightIn here. ModalBottomSheet calculates its expanded
        // anchor from the CONTENT's measured height. Applying heightIn to the sheet
        // itself only constraints the composable layout, not the drag anchor — this
        // causes an empty gap at the bottom rather than a gap at the top.
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topInset + 20.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        ImmersiveMode()

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset = available

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity = available
            }
        }

        // heightIn(max) on the CONTENT Column is what actually caps the sheet's
        // expanded anchor position. The sheet sizes itself to fit its content,
        // so capping content height = capping sheet height = gap visible at the top.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxContentHeight)
                .navigationBarsPadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                val isDark = when (settings.appTheme) {
                    AppTheme.DARK -> true
                    AppTheme.LIGHT -> false
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
                IconButton(
                    onClick = {
                        val nextTheme = if (isDark) AppTheme.LIGHT else AppTheme.DARK
                        onUpdateSettings { it.copy(appTheme = nextTheme) }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // --- Game Rules & Flow ---
            Text(
                text = "Game Rules & Flow",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            SettingsItem(
                title = "Strict Turn Mode",
                subtitle = if (settings.enforceStrictMode) "Rules Locked (In Settings)" 
                           else if (settings.strictTurnMode) "Security Active" 
                           else "Quick play enabled",
                icon = Icons.Default.Lock,
                trailing = {
                    Switch(
                        enabled = !settings.enforceStrictMode,
                        checked = settings.strictTurnMode,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { 
                                var newSettings = it.copy(strictTurnMode = enabled)
                                if (enabled) {
                                    newSettings = newSettings.copy(autoNextTurn = true)
                                }
                                newSettings
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.error,
                            checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                        )
                    )
                }
            )

            SettingsItem(
                title = "Auto-Advance Turn",
                subtitle = "Switch player after scoring",
                icon = Icons.Default.FastForward,
                trailing = {
                    Switch(
                        enabled = !settings.strictTurnMode, 
                        checked = settings.autoNextTurn || settings.strictTurnMode,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(autoNextTurn = enabled) }
                        }
                    )
                }
            )

            SettingsItem(
                title = "Pool Ball Management",
                subtitle = "Insights & elimination for billiards",
                icon = Icons.Default.SportsEsports, // Can use another icon if imported
                trailing = {
                    Switch(
                        checked = settings.poolBallManagementEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(poolBallManagementEnabled = enabled) }
                        }
                    )
                }
            )

            if (settings.poolBallManagementEnabled) {
                SettingsItem(
                    title = "Auto-Remove Pool Balls",
                    subtitle = "Removes matching ball upon positive score",
                    icon = Icons.Default.AutoAwesome,
                    trailing = {
                        Switch(
                            checked = settings.autoRemovePoolBalls,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(autoRemovePoolBalls = enabled) }
                            }
                        )
                    }
                )

                if (!settings.strictTurnMode) {
                    SettingsItem(
                        title = "Allow Eliminated Input",
                        subtitle = "Eliminated players keep their turns",
                        icon = Icons.Default.Help,
                        trailing = {
                            Switch(
                                checked = settings.allowEliminatedInput,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings { it.copy(allowEliminatedInput = enabled) }
                                }
                            )
                        }
                    )
                }
            }

            // --- Display & Interface ---
            Text(
                text = "Display & Interface",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Scoreboard Layout", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settings.defaultLayout == ScoreboardLayout.LIST) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { /* Already selected */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.ViewAgenda, null)
                            Spacer(Modifier.width(8.dp))
                            Text("List")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onUpdateSettings { it.copy(defaultLayout = ScoreboardLayout.LIST) }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.ViewAgenda, null)
                            Spacer(Modifier.width(8.dp))
                            Text("List")
                        }
                    }

                    if (settings.defaultLayout == ScoreboardLayout.GRID) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { /* Already selected */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.GridView, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Grid")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onUpdateSettings { it.copy(defaultLayout = ScoreboardLayout.GRID) }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.GridView, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Grid")
                        }
                    }
                }
            }

            SettingsItem(
                title = "Spotlights",
                subtitle = "Highlight leader and loser",
                icon = Icons.Default.Palette,
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

            SettingsItem(
                title = "Auto-Scroll to Active",
                subtitle = "Keep current player in view",
                icon = Icons.Default.FastForward,
                trailing = {
                    Switch(
                        checked = settings.autoScrollToActivePlayer,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(autoScrollToActivePlayer = enabled) }
                        }
                    )
                }
            )

            SettingsItem(
                title = "Custom Numpad",
                subtitle = "Use minimal keyboard for scoring",
                icon = Icons.Default.Keyboard,
                trailing = {
                    Switch(
                        checked = settings.useCustomKeyboard,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(useCustomKeyboard = enabled) }
                        }
                    )
                }
            )

            SettingsItem(
                title = "Show Help Link",
                subtitle = "Toggle Help icon in the bottom bar",
                icon = Icons.Default.Help,
                trailing = {
                    Switch(
                        checked = settings.showHelpInNavBar,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(showHelpInNavBar = enabled) }
                        }
                    )
                }
            )
        }
    }
}

