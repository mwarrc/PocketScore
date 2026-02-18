package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.KeyboardTheme
import com.mwarrc.pocketscore.domain.model.KeyboardHeight
import com.mwarrc.pocketscore.ui.theme.PocketScoreTheme
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
    onDismiss: () -> Unit,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
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
                            // Dismiss if dragged far enough OR flicked with enough speed
                            if (offsetY.value > 150f || velocity > 1200f) {
                                offsetY.animateTo(
                                    targetValue = 1000f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
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
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Elegant Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Settings
                    FilledTonalIconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Center: Prominent Drag Handle
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                RoundedCornerShape(2.dp)
                            )
                    )

                    // Right: Pin & Dismiss Group
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isPinned) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (isPinned) "Unpin" else "Pin",
                                tint = if (isPinned) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Hide",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                
                val keys: List<List<String>> = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(".", "0", "backspace")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                    onClick = {
                                        if (settings.hapticFeedbackEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        when (key) {
                                            "backspace" -> onBackspaceClick()
                                            else -> onNumberClick(key)
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
    onClick: () -> Unit
) {
    val isAction = key == "backspace"
    
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isAction) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (isAction)
                MaterialTheme.colorScheme.error
            else if (isDark)
                Color.White
            else
                MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (key == "backspace") {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                modifier = Modifier.size((textSize + 2).dp)
            )
        } else {
            Text(
                text = key,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = textSize.sp,
                    fontWeight = FontWeight.Bold
                )
            )
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
                valueRange = 18f..32f,
                steps = 13
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
