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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
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
                // A fixed-size slot in every state. This used to swap between wide
                // text chips ("Already in match", "Continuing Record", …) and a 36dp
                // box, which resized the row and shoved the text field sideways as
                // you typed. The descriptive text now lives on its own line below.
                val badgeBackground = when {
                    justAdded   -> colorScheme.tertiaryContainer
                    isDuplicate -> colorScheme.errorContainer
                    isSaved     -> colorScheme.secondaryContainer
                    hasText     -> colorScheme.primaryContainer
                    else        -> colorScheme.surfaceVariant
                }
                val badgeContent = when {
                    justAdded   -> colorScheme.onTertiaryContainer
                    isDuplicate -> colorScheme.onErrorContainer
                    isSaved     -> colorScheme.onSecondaryContainer
                    hasText     -> colorScheme.onPrimaryContainer
                    else        -> colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }

                Surface(shape = RoundedCornerShape(8.dp), color = badgeBackground) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = justAdded,
                            transitionSpec = {
                                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.75f))
                                    .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.75f))
                            },
                            label = "badge"
                        ) { added ->
                            if (added) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = badgeContent,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = trimmed.firstOrNull()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (hasText) FontWeight.Black else FontWeight.Medium,
                                    color = badgeContent
                                )
                            }
                        }
                    }
                }

                // ── Text field --─
                BasicTextField(
                    value = name,
                    onValueChange = { onNameChange(it.take(26)) },
                    modifier = if (focusRequester != null) {
                        Modifier.weight(1f).focusRequester(focusRequester)
                    } else {
                        Modifier.weight(1f)
                    },
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
                                    "Enter Player Name...",
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
                Button(
                    onClick = { doAdd() },
                    enabled = canAdd || justAdded,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (justAdded) colorScheme.tertiary else colorScheme.primary,
                        contentColor = if (justAdded) colorScheme.onTertiary else colorScheme.onPrimary,
                        disabledContainerColor = colorScheme.surfaceContainerHighest,
                        disabledContentColor = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = justAdded,
                        transitionSpec = {
                            (fadeIn(tween(150)) + scaleIn()) togetherWith (fadeOut(tween(150)) + scaleOut())
                        },
                        label = "button_content"
                    ) { added ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (added) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (added) "Added" else "Add",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── Helper line ──────────────────────────────────────────────────────
        // Carries the status that the leading badge used to spell out inline,
        // so the field itself never has to change width.
        val helper = when {
            justAdded   -> "Added to the line-up" to colorScheme.onTertiaryContainer
            isDuplicate -> "Already in the line-up" to colorScheme.error
            isSaved     -> "Continuing an existing record" to colorScheme.secondary
            else        -> null
        }

        AnimatedVisibility(
            visible = helper != null,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120)),
        ) {
            // Keep the previous text mounted while it animates out.
            val (text, color) = helper ?: ("" to colorScheme.onSurfaceVariant)
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
