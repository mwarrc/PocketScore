package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.KeyboardHeight
import com.mwarrc.pocketscore.domain.model.KeyboardTheme
import com.mwarrc.pocketscore.ui.theme.PocketScoreTheme
import com.mwarrc.pocketscore.ui.util.ImmersiveMode
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A custom numeric keyboard specifically designed for pool scoring.
 *
 * Includes high-quality haptic feedback, physics-based drag-to-dismiss
 * gestures, and an integrated configuration sheet for theme/size adjustment.
 *
 * @param onNumberClick Callback when a numeric or decimal key is pressed
 * @param onBackspaceClick Callback for character deletion
 * @param onDismiss Callback to close the keyboard
 * @param isPinned Whether the keyboard should remain in view
 * @param onTogglePin Callback to change the pinning state
 * @param settings Current application configuration
 * @param onUpdateSettings Callback to modify configuration
 * @param modifier Root modifier for layout adjustments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreNumpad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onDismiss: () -> Unit,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    showDecimal: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val isDark = when (settings.keyboardTheme) {
        KeyboardTheme.LIGHT -> false
        KeyboardTheme.DARK -> true
        KeyboardTheme.AUTO -> isSystemInDarkTheme()
    }

    val buttonHeight = when (settings.keyboardHeight) {
        KeyboardHeight.COMPACT -> 48.dp
        KeyboardHeight.MEDIUM -> 56.dp
        KeyboardHeight.LARGE -> 64.dp
    }

    PocketScoreTheme(darkTheme = isDark) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetY.snapTo((offsetY.value + delta).coerceAtLeast(0f))
                        }
                    },
                    orientation = Orientation.Vertical,
                    enabled = !isPinned,
                    onDragStopped = { velocity ->
                        scope.launch {
                            if (offsetY.value > 150f || velocity > 1200f) {
                                // FIX (Bug 2): Reset offsetY to 0 so AnimatedVisibility in
                                // NumpadOverlay performs the single exit animation cleanly,
                                // instead of both competing and leaving a ghost layer behind.
                                offsetY.snapTo(0f)
                                onDismiss()
                            } else {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    }
                ),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RectangleShape,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Left: Settings
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSettings = true
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .align(Alignment.CenterStart),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Center: Pill Handle
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                CircleShape
                            )
                    )

                    // Right: Pin & Dismiss
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTogglePin()
                            },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isPinned)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(
                                if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (isPinned) "Unpin" else "Pin",
                                tint = if (isPinned)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Hide",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                val keys: List<List<String>> = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(if (showDecimal) "." else "C", "0", "backspace")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                NumpadKey(
                                    key = key,
                                    textSize = settings.keyboardTextSize,
                                    isDark = isDark,
                                    showBorders = settings.keyboardBordersEnabled,
                                    onClick = {
                                        if (settings.hapticFeedbackEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                                        }
                                        when (key) {
                                            "backspace" -> onBackspaceClick()
                                            "C" -> onClearClick()
                                            else -> if (key.isNotEmpty()) onNumberClick(key)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        KeyboardSettingsSheet(
            settings = settings,
            onUpdateSettings = onUpdateSettings,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun NumpadKey(
    key: String,
    textSize: Float,
    isDark: Boolean,
    showBorders: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "key_scale"
    )

    val isAction = key == "backspace" || key == "C"
    val keyShape = RoundedCornerShape(12.dp)

    val baseContainerColor = if (isAction)
        MaterialTheme.colorScheme.errorContainer.copy(alpha = if (key == "C") 0.15f else 0.3f)
    else
        MaterialTheme.colorScheme.surfaceContainerHighest

    val contentColor = if (isAction)
        MaterialTheme.colorScheme.error
    else if (isDark)
        Color.White
    else
        MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (showBorders) Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = keyShape
                ) else Modifier
            )
            .padding(2.dp) // Subtle gap between keys for better touch targets
            .clip(keyShape) // IMPORTANT: Clips both background AND ripple
            .background(if (key.isEmpty()) Color.Transparent else baseContainerColor)
            .clickable(
                enabled = key.isNotEmpty(),
                onClick = onClick,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = contentColor)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Content with a very subtle scale on press
        Box(
            modifier = Modifier.scale(scale), 
            contentAlignment = Alignment.Center
        ) {
            if (key == "backspace") {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size((textSize + 2).dp),
                    tint = contentColor
                )
            } else {
                Text(
                    text = key,
                    color = contentColor,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = (if (key == "." || key == "C") textSize + 2 else textSize).sp,
                        fontWeight = if (key == "C") FontWeight.Black else FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyboardSettingsSheet(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        ImmersiveMode()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Keyboard Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            Text("Theme", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeyboardTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = settings.keyboardTheme == theme,
                        onClick = { onUpdateSettings { it.copy(keyboardTheme = theme) } },
                        label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Text Size: ${settings.keyboardTextSize.toInt()}sp", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = settings.keyboardTextSize,
                onValueChange = { newSize -> onUpdateSettings { it.copy(keyboardTextSize = newSize) } },
                valueRange = 24f..52f,
                steps = 20
            )

            Spacer(Modifier.height(16.dp))

            Text("Button Height", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeyboardHeight.entries.forEach { height ->
                    FilterChip(
                        selected = settings.keyboardHeight == height,
                        onClick = { onUpdateSettings { it.copy(keyboardHeight = height) } },
                        label = { Text(height.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

/*
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Borders", style = MaterialTheme.typography.labelLarge)
                Switch(
                    checked = settings.keyboardBordersEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(keyboardBordersEnabled = enabled) }
                    }
                )
            }
*/

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}