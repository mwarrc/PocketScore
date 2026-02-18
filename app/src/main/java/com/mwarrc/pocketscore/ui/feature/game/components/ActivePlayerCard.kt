package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.Player
import androidx.compose.ui.platform.LocalSoftwareKeyboardController


/**
 * Interactive card for a player whose score can be currently modified.
 * 
 * This component is used in the List layout for every player and as the 
 * header in Grid layout for the focused player.
 * 
 * @param player The player data for this card
 * @param isLeader Whether this player is currently winning
 * @param isTie Whether the lead is shared with others
 * @param isLoser Whether this player is currently in last place
 * @param isLoserTie Whether the last place is shared with others
 * @param isCurrentTurn Whether it is currently this player's active turn
 * @param isStrictTurnMode Whether the app is enforcing turn-based input
 * @param allowEliminatedInput Whether players out of contention can still receive scores
 * @param onAdd Callback for adding a specific amount of points
 * @param onSubtract Callback for subtracting a specific amount of points
 * @param onSetTurn Optional callback to set this player as the active turn holder
 * @param scoreInput Current text buffered for the "next points" entry
 * @param onScoreInputChange Callback when entry text changes
 * @param onFocus Callback when the card is interacted with (to show numpad)
 * @param useCustomKeyboard Whether to use the in-app numpad vs system IME
 * @param modifier Root modifier for the card
 * @param lastPoints The last score change recorded for this player
 * @param alwaysShowControls If true, entry fields are visible even if it's not their turn
 * @param leaderScore Current leader's score for elimination logic
 * @param tableSum Current sum on table for elimination logic
 * @param poolBallManagementEnabled Whether to perform contender math
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivePlayerCard(
    player: Player,
    isLeader: Boolean,
    isTie: Boolean = false,
    isLoser: Boolean = false,
    isLoserTie: Boolean = false,
    isCurrentTurn: Boolean,
    isStrictTurnMode: Boolean,
    allowEliminatedInput: Boolean = false,
    onAdd: (Int) -> Unit,
    onSubtract: (Int) -> Unit,
    onSetTurn: (() -> Unit)? = null,
    scoreInput: String = "",
    onScoreInputChange: (String) -> Unit = {},
    onFocus: () -> Unit = {},
    useCustomKeyboard: Boolean = true,
    modifier: Modifier = Modifier,
    lastPoints: Int? = null,
    alwaysShowControls: Boolean = false,
    leaderScore: Int = 0,
    tableSum: Int = 0,
    poolBallManagementEnabled: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Calculate if player is eliminated - Only if pool management is enabled
    val potentialMax = player.score + tableSum
    val isEliminated = poolBallManagementEnabled && !isLeader && potentialMax < leaderScore && tableSum > 0
    
    // Logic for editing:
    // 1. In Strict Mode: Must be current turn AND not eliminated.
    // 2. In Non-Strict Mode: Everyone can edit, but eliminated players depend on allowEliminatedInput setting.
    val canEdit = if (isStrictTurnMode) {
        isCurrentTurn && !isEliminated
    } else {
        !isEliminated || allowEliminatedInput
    }


    // Snappy auto-focus for system keyboard to prevent it from closing on player switch
    LaunchedEffect(isCurrentTurn, useCustomKeyboard) {
        if (isCurrentTurn && !useCustomKeyboard && canEdit) {
            focusRequester.requestFocus()
        }
    }

    val containerColor = when {
        isEliminated -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        isCurrentTurn -> MaterialTheme.colorScheme.primaryContainer
        isLeader -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        isLoser -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isCurrentTurn -> MaterialTheme.colorScheme.onPrimaryContainer
        isLeader -> MaterialTheme.colorScheme.onTertiaryContainer
        isLoser -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderStroke = when {
        isEliminated -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
        isCurrentTurn -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = canEdit) {
                onSetTurn?.invoke()
                if (!useCustomKeyboard) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                onFocus()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Eliminated overlay effect
            if (isEliminated) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.5f)
                )
                
                // Eliminated icon watermark
                Icon(
                    androidx.compose.material.icons.Icons.Default.Block,
                    contentDescription = "Eliminated",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp)
                        .alpha(0.1f),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            
            if (isLeader && !isCurrentTurn && !isEliminated) {
                val starTint = MaterialTheme.colorScheme.tertiary
                Box(modifier = Modifier.matchParentSize()) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(28.dp).align(Alignment.TopStart)
                            .offset(16.dp, 16.dp).alpha(0.18f).rotate(-15f),
                        tint = starTint
                    )
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(20.dp).align(Alignment.TopEnd)
                            .offset((-60).dp, 12.dp).alpha(0.12f).rotate(10f),
                        tint = starTint
                    )
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                            .offset((-12).dp, 20.dp).alpha(0.15f).rotate(25f),
                        tint = starTint
                    )
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(16.dp).align(Alignment.CenterStart)
                            .offset(40.dp, (-20).dp).alpha(0.1f).rotate(5f),
                        tint = starTint
                    )
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(22.dp).align(Alignment.BottomStart)
                            .offset(32.dp, (-16).dp).alpha(0.12f).rotate(-10f),
                        tint = starTint
                    )
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(26.dp).align(Alignment.BottomEnd)
                            .offset((-20).dp, (-12).dp).alpha(0.18f).rotate(-5f),
                        tint = starTint
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLeader || isLoser) {
                                val badgeColor = if (isLeader) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                                val onBadgeColor = if (isLeader) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                val label = when {
                                    isLeader && isTie -> "TIED"
                                    isLeader -> "LEADER"
                                    isLoser && isLoserTie -> "TIED"
                                    else -> "LOSER"
                                }
                                val icon = when {
                                    isLeader && isTie -> Icons.Default.Group
                                    isLeader -> Icons.Default.EmojiEvents
                                    else -> null
                                }

                                Surface(
                                    color = badgeColor,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (icon != null) {
                                            Icon(
                                                icon,
                                                contentDescription = label,
                                                modifier = Modifier.size(14.dp),
                                                tint = onBadgeColor
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = onBadgeColor
                                        )
                                    }
                                }
                            }
                            Text(
                                player.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = if (isLeader || isCurrentTurn) FontWeight.Black else FontWeight.Bold,
                                color = contentColor
                            )
                        }
                        when {
                            isCurrentTurn -> {
                                Surface(
                                    color = if (isLeader) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        " PLAYING NOW ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isLeader) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 4.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }

                            isStrictTurnMode -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        "Locked",
                                        modifier = Modifier.size(12.dp),
                                        tint = contentColor.copy(alpha = 0.7f)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Turn Locked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = contentColor.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            onSetTurn != null -> {
                                Text(
                                    "Tap to Play",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        
                        // Eliminated badge
                        if (isEliminated) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    " ELIMINATED ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 4.dp,
                                        vertical = 2.dp
                                    )
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        if (lastPoints != null) {
                            val pointsColor = when {
                                lastPoints > 0 -> Color(0xFF4CAF50)
                                lastPoints < 0 -> MaterialTheme.colorScheme.error
                                else -> contentColor.copy(alpha = 0.5f)
                            }
                            val pointsText = when {
                                lastPoints > 0 -> "+$lastPoints"
                                lastPoints < 0 -> "$lastPoints"
                                else -> "0"
                            }
                            Surface(
                                color = pointsColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
                            ) {
                                Text(
                                    text = pointsText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = pointsColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            "${player.score}",
                            style = if (isCurrentTurn) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = contentColor
                        )
                    }
                }

                // Snappy animations (200ms)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isCurrentTurn || alwaysShowControls,
                    enter = androidx.compose.animation.expandVertically(animationSpec = tween(200)) + 
                            androidx.compose.animation.fadeIn(animationSpec = tween(200)),
                    exit = androidx.compose.animation.shrinkVertically(animationSpec = tween(200)) + 
                           androidx.compose.animation.fadeOut(animationSpec = tween(200))
                ) {
                    Column {
                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = canEdit) {
                                        onSetTurn?.invoke()
                                        onFocus()
                                        if (!useCustomKeyboard) {
                                            focusRequester.requestFocus()
                                            keyboardController?.show()
                                        }
                                    },
                                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (canEdit) 1f else 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            ) {
                                if (!useCustomKeyboard && canEdit) {
                                    BasicTextField(
                                        value = scoreInput,
                                        onValueChange = onScoreInputChange,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .focusRequester(focusRequester),
                                        textStyle = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 22.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (scoreInput.isEmpty()) {
                                                    Text(
                                                        "+ pts",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.5f
                                                        )
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (scoreInput.isEmpty()) {
                                            Text(
                                                "+ pts",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                        } else {
                                            Text(
                                                scoreInput,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 22.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isDark = isSystemInDarkTheme()

                                 FilledIconButton(
                                    onClick = {
                                        if (!isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                                        val points = scoreInput.toIntOrNull() ?: 0
                                        onSubtract(points)
                                        onScoreInputChange("")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = canEdit && scoreInput.isNotEmpty(),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (isDark) Color(0xFF8C1D18) else MaterialTheme.colorScheme.error, // Prominent red
                                        contentColor = if (isDark) Color(0xFFFFB4AB) else MaterialTheme.colorScheme.onError,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                ) {
                                    Icon(Icons.Outlined.Remove, "Subtract")
                                }

                                FilledIconButton(
                                    onClick = {
                                        if (!isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                                        val points = scoreInput.toIntOrNull() ?: 0
                                        onAdd(points)
                                        onScoreInputChange("")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = canEdit && scoreInput.isNotEmpty(),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                ) {
                                    Icon(Icons.Outlined.Add, "Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}