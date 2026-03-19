package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Material 3 styled player quick-add field.
 *
 * Visual states:
 *  - Empty      → muted label icon, neutral outline
 *  - Typing     → live initial badge (primary), active primary outline, solid add button
 *  - Duplicate  → error colour on outline + hint text, button disabled
 *  - Just added → 800ms green ✓ flash before auto-reset
 */
@Composable
fun PlayerInputSection(
    name: String,
    onNameChange: (String) -> Unit,
    onAddPlayer: () -> Unit,
    savedNames: List<String> = emptyList(),
    selectedNames: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val trimmed = name.trim()
    val hasText = trimmed.isNotEmpty()
    
    // It's a duplicate only if the player is already added to the CURRENT match being set up
    val isDuplicate = hasText && selectedNames.any { it.equals(trimmed, ignoreCase = true) }
    // It's a saved player if they are in the database, meaning their record is continuing
    val isSaved = hasText && !isDuplicate && savedNames.any { it.equals(trimmed, ignoreCase = true) }
    
    val canAdd = hasText && !isDuplicate

    var justAdded by remember { mutableStateOf(false) }

    val doAdd = {
        if (canAdd) {
            onAddPlayer()
            justAdded = true
        }
    }

    LaunchedEffect(justAdded) {
        if (justAdded) { delay(800); justAdded = false }
    }

    // ── colour tokens --──────────
    val borderStroke = when {
        justAdded   -> androidx.compose.foundation.BorderStroke(1.dp, colorScheme.tertiary.copy(alpha = 0.6f))
        isDuplicate -> androidx.compose.foundation.BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.6f))
        isSaved     -> androidx.compose.foundation.BorderStroke(1.dp, colorScheme.secondary.copy(alpha = 0.6f))
        hasText     -> androidx.compose.foundation.BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.4f))
        else        -> null // Smooth, borderless idle state
    }
    
    val containerColor = when {
        justAdded   -> colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        isDuplicate -> colorScheme.errorContainer.copy(alpha = 0.25f)
        isSaved     -> colorScheme.secondaryContainer.copy(alpha = 0.25f)
        else        -> colorScheme.surfaceVariant.copy(alpha = 0.7f) // Subdued default background
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {

        // ── Input card --─────────
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = containerColor,
            border = borderStroke,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Leading badge ────────────────────────────────────────────
                AnimatedContent(
                    targetState = when {
                        justAdded   -> "done"
                        isDuplicate -> "dup"
                        isSaved     -> "saved"
                        hasText     -> "letter"
                        else        -> "idle"
                    },
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.75f))
                            .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.75f))
                    },
                    label = "badge"
                ) { state ->
                    when (state) {
                        "done" -> AssistChip(
                            onClick = {},
                            label = { Text("✓  Added", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colorScheme.tertiaryContainer,
                                labelColor = colorScheme.onTertiaryContainer
                            ),
                            border = null
                        )
                        "dup" -> AssistChip(
                            onClick = {},
                            label = { Text("Already in match", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colorScheme.errorContainer,
                                labelColor = colorScheme.onErrorContainer
                            ),
                            border = null
                        )
                        "saved" -> AssistChip(
                            onClick = {},
                            label = { Text("Continuing Record", fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colorScheme.secondaryContainer,
                                labelColor = colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )
                        "letter" -> Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colorScheme.primaryContainer
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = trimmed.firstOrNull()?.uppercase() ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black, fontSize = 16.sp
                                    ),
                                    color = colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        else -> Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "?",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium, fontSize = 16.sp
                                    ),
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // ── Text field --─
                BasicTextField(
                    value = name,
                    onValueChange = { onNameChange(it.take(26)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    cursorBrush = SolidColor(colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { doAdd() }),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    ),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (name.isEmpty()) {
                                Text(
                                    "Enter player name...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            }
                            inner()
                        }
                    }
                )

                // ── Add button --─
                FilledIconButton(
                    onClick = { doAdd() },
                    enabled = canAdd,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                        disabledContainerColor = colorScheme.surfaceContainerHighest,
                        disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add player",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
