package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.AppTheme
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import com.mwarrc.pocketscore.ui.util.ImmersiveMode

/**
 * A compact bottom sheet for toggling common game-related settings mid-match.
 *
 * Styled like a mobile quick-settings panel:
 *  - Capped at ~66% of the screen height so it never covers the whole scoreboard.
 *  - The scrim (empty area above the panel) closes it on tap.
 *  - Settings are grouped into rounded cards per section.
 *
 * @param initialSettings Current application configuration
 * @param onUpdateSettingsBase Callback to modify one or more settings fields
 * @param onDismiss Callback to close the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsSheet(
    initialSettings: AppSettings,
    onUpdateSettingsBase: ((AppSettings) -> AppSettings) -> Unit,
    onLiveScaleChange: (Float) -> Unit = {},
    onDismiss: () -> Unit
) {
    var settings by remember(initialSettings) { mutableStateOf(initialSettings) }
    var isPeeking by remember { mutableStateOf(false) }
    var initialPeekScale by remember { mutableFloatStateOf(initialSettings.globalScale) }
    var localScale by remember { mutableFloatStateOf(initialSettings.globalScale) }
    var showDisablePoolDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialSettings.globalScale) {
        if (!isPeeking) {
            localScale = initialSettings.globalScale
        }
    }

    val handleUpdateSettings: ((AppSettings) -> AppSettings) -> Unit = { updater ->
        val updated = updater(settings)
        settings = updated
        onUpdateSettingsBase { updater(it) }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // Cap shared with every other sheet on this screen: at most 75% of the
    // physical screen, so a quarter always stays visible for context and
    // tap-to-dismiss. The old value here was 0.85f, and other sheets sat
    // anywhere between that and the full screen.
    val maxPanelHeight = rememberGameSheetMaxHeight()

    // Let the sheet be dragged down (and dismissed) once the content can't scroll
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

    val animatedScrimColor by animateColorAsState(
        targetValue = if (isPeeking) Color.Transparent else BottomSheetDefaults.ScrimColor,
        animationSpec = tween(280),
        label = "scrimColor"
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isPeeking) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
                      else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(280),
        label = "containerColor"
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (isPeeking) {
                localScale = initialPeekScale
                onLiveScaleChange(initialPeekScale)
                isPeeking = false
                coroutineScope.launch {
                    try { sheetState.expand() } catch (e: Exception) {}
                }
            } else {
                onDismiss()
            }
        },
        sheetState = sheetState,
        scrimColor = animatedScrimColor,
        containerColor = animatedContainerColor,
        dragHandle = {
            // Deliberately no displayCutoutPadding() here. It used to be applied
            // on the drag handle AND again on the content column below, and in
            // immersive mode the status-bar inset is zero while the cutout
            // inset is still real - so the two stacked into a dead band of empty
            // space at the top of the sheet. The handle is centred purely
            // horizontally, so it needs no inset handling at all: the sheet's
            // own surface already starts below the cutout.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(if (isPeeking) 32.dp else 40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) {
        ImmersiveMode()

        AnimatedContent(
            targetState = isPeeking,
            transitionSpec = {
                (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.96f)) togetherWith
                (fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.96f))
            },
            label = "QuickSettingsPeekTransition"
        ) { peeking ->
            if (peeking) {
                PeekScaleDock(
                    scale = localScale,
                    onScaleChange = { newScale ->
                        localScale = newScale
                        onLiveScaleChange(newScale)
                    },
                    onScaleCommit = { committedScale ->
                        localScale = committedScale
                        handleUpdateSettings { it.copy(globalScale = committedScale) }
                    },
                    onDone = {
                        handleUpdateSettings { it.copy(globalScale = localScale) }
                        isPeeking = false
                        coroutineScope.launch {
                            try { sheetState.expand() } catch (e: Exception) {}
                        }
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxPanelHeight)
                        .navigationBarsPadding()
                        .nestedScroll(nestedScrollConnection)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // --- Header ---
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isDark = when (settings.appTheme) {
                        AppTheme.DARK -> true
                        AppTheme.LIGHT -> false
                        AppTheme.SYSTEM -> isSystemInDarkTheme()
                    }
                    IconButton(
                        onClick = {
                            val nextTheme = if (isDark) AppTheme.LIGHT else AppTheme.DARK
                            handleUpdateSettings { it.copy(appTheme = nextTheme) }
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

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Menu")
                    }
                }
            }

            // --- Game Rules & Flow ---
            SectionLabel("Game Rules & Flow")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
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
                                    handleUpdateSettings {
                                        var newSettings = it.copy(strictTurnMode = enabled)
                                        if (enabled) {
                                            newSettings = newSettings.copy(
                                                autoNextTurn = true,
                                                autoAdvanceOnNegativeOnly = false
                                            )
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

                    CardDivider()

                    SettingsItem(
                        title = "Auto-Advance Turn",
                        subtitle = "Switch player after scoring",
                        icon = Icons.Default.FastForward,
                        trailing = {
                            Switch(
                                enabled = !settings.strictTurnMode,
                                checked = settings.autoNextTurn || settings.strictTurnMode,
                                onCheckedChange = { enabled ->
                                    handleUpdateSettings { it.copy(autoNextTurn = enabled) }
                                }
                            )
                        }
                    )

                    if (settings.autoNextTurn || settings.strictTurnMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Auto-Advance Trigger",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!settings.autoAdvanceOnNegativeOnly) {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = { /* Already selected */ },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Any Score")
                                    }
                                } else {
                                    OutlinedButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            handleUpdateSettings { it.copy(autoAdvanceOnNegativeOnly = false) }
                                        },
                                        enabled = !settings.strictTurnMode,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Any Score")
                                    }
                                }

                                if (settings.autoAdvanceOnNegativeOnly) {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = { /* Already selected */ },
                                        enabled = !settings.strictTurnMode,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Negative Only")
                                    }
                                } else {
                                    OutlinedButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            handleUpdateSettings { it.copy(autoAdvanceOnNegativeOnly = true) }
                                        },
                                        enabled = !settings.strictTurnMode,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Negative Only")
                                    }
                                }
                            }
                        }

                        CardDivider()
                    }

                    SettingsItem(
                        title = "Pool Ball Management",
                        subtitle = "Insights & elimination for billiards",
                        icon = Icons.Default.SportsEsports,
                        trailing = {
                            Switch(
                                checked = settings.poolBallManagementEnabled,
                                onCheckedChange = { enabled ->
                                    if (!enabled) {
                                        showDisablePoolDialog = true
                                    } else {
                                        handleUpdateSettings { it.copy(poolBallManagementEnabled = true) }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    uncheckedThumbColor = if (!settings.poolBallManagementEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                                    uncheckedTrackColor = if (!settings.poolBallManagementEnabled) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = if (!settings.poolBallManagementEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                        }
                    )

                    if (settings.poolBallManagementEnabled) {
                        CardDivider()

                        SettingsItem(
                            title = "Auto-Remove Pool Balls",
                            subtitle = "Removes matching ball upon positive score",
                            icon = Icons.Default.AutoAwesome,
                            trailing = {
                                Switch(
                                    checked = settings.autoRemovePoolBalls,
                                    onCheckedChange = { enabled ->
                                        handleUpdateSettings { it.copy(autoRemovePoolBalls = enabled) }
                                    }
                                )
                            }
                        )

                        if (!settings.strictTurnMode) {
                            CardDivider()

                            SettingsItem(
                                title = "Allow Eliminated Input",
                                subtitle = "Eliminated players keep their turns",
                                icon = Icons.AutoMirrored.Filled.Help,
                                trailing = {
                                    Switch(
                                        checked = settings.allowEliminatedInput,
                                        onCheckedChange = { enabled ->
                                            handleUpdateSettings { it.copy(allowEliminatedInput = enabled) }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // --- Display & Interface ---
            SectionLabel("Display & Interface")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("App Scale (Zoom)", style = MaterialTheme.typography.titleMedium)
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = String.format(java.util.Locale.US, "%.2fx", localScale),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Adjust UI size to fit your screen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    initialPeekScale = localScale
                                    isPeeking = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Peek", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Steppers + Slider row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val canStepDown = localScale > AppSettings.MIN_GLOBAL_SCALE + 0.01f
                            val canStepUp = localScale < AppSettings.MAX_GLOBAL_SCALE - 0.01f

                            FilledTonalIconButton(
                                onClick = {
                                    if (canStepDown) {
                                        val next = (Math.round((localScale - 0.05f) * 20f) / 20f)
                                            .coerceIn(AppSettings.MIN_GLOBAL_SCALE, AppSettings.MAX_GLOBAL_SCALE)
                                        localScale = next
                                        onLiveScaleChange(next)
                                        handleUpdateSettings { it.copy(globalScale = next) }
                                    }
                                },
                                enabled = canStepDown,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease scale", modifier = Modifier.size(18.dp))
                            }

                            Slider(
                                value = localScale,
                                onValueChange = { raw ->
                                    val stepped = (Math.round(raw * 20f) / 20f)
                                        .coerceIn(AppSettings.MIN_GLOBAL_SCALE, AppSettings.MAX_GLOBAL_SCALE)
                                    localScale = stepped
                                    onLiveScaleChange(stepped)
                                },
                                onValueChangeFinished = {
                                    handleUpdateSettings { it.copy(globalScale = localScale) }
                                },
                                valueRange = AppSettings.MIN_GLOBAL_SCALE..AppSettings.MAX_GLOBAL_SCALE,
                                steps = 13,
                                modifier = Modifier.weight(1f)
                            )

                            FilledTonalIconButton(
                                onClick = {
                                    if (canStepUp) {
                                        val next = (Math.round((localScale + 0.05f) * 20f) / 20f)
                                            .coerceIn(AppSettings.MIN_GLOBAL_SCALE, AppSettings.MAX_GLOBAL_SCALE)
                                        localScale = next
                                        onLiveScaleChange(next)
                                        handleUpdateSettings { it.copy(globalScale = next) }
                                    }
                                },
                                enabled = canStepUp,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase scale", modifier = Modifier.size(18.dp))
                            }
                        }

                        // Presets & Reset row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val presets = listOf(0.80f, 1.00f, 1.15f, 1.30f)
                                presets.forEach { preset ->
                                    val isSelected = kotlin.math.abs(localScale - preset) < 0.02f
                                    Surface(
                                        onClick = {
                                            localScale = preset
                                            onLiveScaleChange(preset)
                                            handleUpdateSettings { it.copy(globalScale = preset) }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Text(
                                            text = if (preset == 1.00f) "1.00x (Default)" else String.format(java.util.Locale.US, "%.2fx", preset),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            val isDefault = kotlin.math.abs(localScale - 1.0f) < 0.02f
                            if (!isDefault) {
                                TextButton(
                                    onClick = {
                                        localScale = 1.0f
                                        onLiveScaleChange(1.0f)
                                        handleUpdateSettings { it.copy(globalScale = 1.0f) }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reset", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    CardDivider()

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Scoreboard Layout",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        handleUpdateSettings { it.copy(defaultLayout = ScoreboardLayout.LIST) }
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
                                        handleUpdateSettings { it.copy(defaultLayout = ScoreboardLayout.GRID) }
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

                    CardDivider()

                    SettingsItem(
                        title = "Spotlights",
                        subtitle = "Highlight leader and loser",
                        icon = Icons.Default.Palette,
                        trailing = {
                            Switch(
                                checked = settings.leaderSpotlightEnabled || settings.loserSpotlightEnabled,
                                onCheckedChange = { enabled ->
                                    handleUpdateSettings {
                                        it.copy(
                                            leaderSpotlightEnabled = enabled,
                                            loserSpotlightEnabled = enabled
                                        )
                                    }
                                }
                            )
                        }
                    )

                    CardDivider()

                    SettingsItem(
                        title = "Auto-Scroll to Active",
                        subtitle = "Keep current player in view",
                        icon = Icons.Default.FastForward,
                        trailing = {
                            Switch(
                                checked = settings.autoScrollToActivePlayer,
                                onCheckedChange = { enabled ->
                                    handleUpdateSettings { it.copy(autoScrollToActivePlayer = enabled) }
                                }
                            )
                        }
                    )

                    CardDivider()

                    SettingsItem(
                        title = "Custom Numpad",
                        subtitle = "Use minimal keyboard for scoring",
                        icon = Icons.Default.Keyboard,
                        trailing = {
                            Switch(
                                checked = settings.useCustomKeyboard,
                                onCheckedChange = { enabled ->
                                    handleUpdateSettings { it.copy(useCustomKeyboard = enabled) }
                                }
                            )
                        }
                    )

                    CardDivider()

                    SettingsItem(
                        title = "Show Help Link",
                        subtitle = "Toggle Help icon in the bottom bar",
                        icon = Icons.AutoMirrored.Filled.Help,
                        trailing = {
                            Switch(
                                checked = settings.showHelpInNavBar,
                                onCheckedChange = { enabled ->
                                    handleUpdateSettings { it.copy(showHelpInNavBar = enabled) }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
    }

    if (showDisablePoolDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDisablePoolDialog = false },
            icon = { Icon(Icons.Default.SportsEsports, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Disable Pool Management?") },
            text = {
                Text("Turning this off will remove the specialized pool tracking interface from your match screen, including ball remaining indicators and automatic point calculation based on balls.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        handleUpdateSettings { it.copy(poolBallManagementEnabled = false) }
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
 * Sleek bottom dock shown when peeking at the game screen.
 * Leaves 85-90% of the screen unobstructed so the user can see live UI scaling
 * behind the dock as they slide or click stepper controls.
 */
@Composable
private fun PeekScaleDock(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onScaleCommit: (Float) -> Unit,
    onDone: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .displayCutoutPadding()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Eye icon, Title & Live Value Pill, Done button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Live Scale Peek",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scaling updates live as you drag",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.2fx", scale),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Slider & Steppers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val canStepDown = scale > AppSettings.MIN_GLOBAL_SCALE + 0.01f
                val canStepUp = scale < AppSettings.MAX_GLOBAL_SCALE - 0.01f

                FilledTonalIconButton(
                    onClick = {
                        if (canStepDown) {
                            val next = (Math.round((scale - 0.05f) * 20f) / 20f)
                                .coerceIn(AppSettings.MIN_GLOBAL_SCALE, AppSettings.MAX_GLOBAL_SCALE)
                            onScaleChange(next)
                            onScaleCommit(next)
                        }
                    },
                    enabled = canStepDown,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease scale")
                }

                Slider(
                    value = scale,
                    onValueChange = { raw ->
                        val stepped = (Math.round(raw * 20f) / 20f)
                            .coerceIn(AppSettings.MIN_GLOBAL_SCALE, AppSettings.MAX_GLOBAL_SCALE)
                        onScaleChange(stepped)
                    },
                    onValueChangeFinished = {
                        onScaleCommit(scale)
                    },
                    valueRange = AppSettings.MIN_GLOBAL_SCALE..AppSettings.MAX_GLOBAL_SCALE,
                    steps = 13,
                    modifier = Modifier.weight(1f)
                )

                FilledTonalIconButton(
                    onClick = {
                        if (canStepUp) {
                            val next = (Math.round((scale + 0.05f) * 20f) / 20f)
                                .coerceIn(AppSettings.MIN_GLOBAL_SCALE, AppSettings.MAX_GLOBAL_SCALE)
                            onScaleChange(next)
                            onScaleCommit(next)
                        }
                    },
                    enabled = canStepUp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase scale")
                }
            }

            // Quick Preset Chips & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(0.80f, 1.00f, 1.15f, 1.30f)
                    presets.forEach { preset ->
                        val isSelected = kotlin.math.abs(scale - preset) < 0.02f
                        Surface(
                            onClick = {
                                onScaleChange(preset)
                                onScaleCommit(preset)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = if (preset == 1.00f) "1.00x Default" else String.format(java.util.Locale.US, "%.2fx", preset),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                val isDefault = kotlin.math.abs(scale - 1.0f) < 0.02f
                if (!isDefault) {
                    TextButton(
                        onClick = {
                            onScaleChange(1.0f)
                            onScaleCommit(1.0f)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reset", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

/** Small section title used above each settings card. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

/** Subtle divider between items inside a settings card. */
@Composable
private fun CardDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
