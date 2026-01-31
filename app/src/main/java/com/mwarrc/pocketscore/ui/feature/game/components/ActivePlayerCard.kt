package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.Player

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivePlayerCard(
    player: Player,
    isLeader: Boolean,
    isCurrentTurn: Boolean,
    isStrictTurnMode: Boolean,
    onAdd: (Int) -> Unit,
    onSubtract: (Int) -> Unit,
    onSetTurn: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    lastPoints: Int? = null
) {
    var inputValue by remember(player.id) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isCurrentTurn) {
        if (isCurrentTurn && isImeVisible) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val canEdit = !isStrictTurnMode || isCurrentTurn

    val containerColor = when {
        isCurrentTurn -> MaterialTheme.colorScheme.primaryContainer
        isLeader -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isCurrentTurn -> MaterialTheme.colorScheme.onPrimaryContainer
        isLeader -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderStroke = if (isCurrentTurn) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    val modifierWithClick = if (canEdit) {
        modifier.clickable {
            onSetTurn?.invoke()
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    } else {
        modifier
    }

    Card(
        modifier = modifierWithClick.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Leader: subtle star field (no border; current-turn card stays the clear focus)
            if (isLeader && !isCurrentTurn) {
                val starTint = MaterialTheme.colorScheme.tertiary
                Box(modifier = Modifier.matchParentSize()) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp).align(Alignment.TopStart).offset(22.dp, 22.dp).alpha(0.22f).rotate(-12f), tint = starTint)
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp).align(Alignment.TopEnd).offset((-28).dp, 26.dp).alpha(0.2f).rotate(8f), tint = starTint)
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp).align(Alignment.BottomEnd).offset((-44).dp, (-22).dp).alpha(0.22f).rotate(-8f), tint = starTint)
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
                            if (isLeader) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = "Leader",
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "In the lead",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                                )
                                Spacer(Modifier.width(8.dp))
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
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                                    color = contentColor.copy(alpha = 0.6f)
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
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = contentColor
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = {
                            if (!isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                            if (it.isEmpty() || it.toIntOrNull() != null) inputValue = it
                        },
                        placeholder = { Text("+ pts", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused && !isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                            },
                        singleLine = true,
                        enabled = canEdit,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        )
                    )

                    FilledTonalIconButton(
                        onClick = {
                            if (!isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                            val points = inputValue.toIntOrNull() ?: 0
                            onSubtract(points)
                            inputValue = ""
                        },
                        modifier = Modifier.size(56.dp),
                        enabled = canEdit && inputValue.isNotEmpty(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(Icons.Outlined.Remove, "Subtract")
                    }

                    FilledIconButton(
                        onClick = {
                            if (!isStrictTurnMode && !isCurrentTurn) onSetTurn?.invoke()
                            val points = inputValue.toIntOrNull() ?: 0
                            onAdd(points)
                            inputValue = ""
                        },
                        modifier = Modifier.size(56.dp),
                        enabled = canEdit && inputValue.isNotEmpty(),
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
