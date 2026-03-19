package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddPlayerDialog(
    existingPlayers: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val trimmed = name.trim()
    val isDuplicate = existingPlayers.any { it.equals(trimmed, ignoreCase = true) }
    val isEnabled = trimmed.isNotEmpty()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        shape = RoundedCornerShape(28.dp),
        containerColor = colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .imePadding(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Icon in a tonal container matching the header logo style
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = colorScheme.primary.copy(alpha = 0.08f),
                                cornerRadius = CornerRadius(10.dp.toPx()),
                            )
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    0f to colorScheme.primary.copy(alpha = 0.30f),
                                    1f to colorScheme.primary.copy(alpha = 0.05f),
                                ),
                                cornerRadius = CornerRadius(10.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = colorScheme.primary.copy(alpha = 0.80f),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Text(
                    text = "Player Setup",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                    ),
                    color = colorScheme.onSurface,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            "Enter name",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    singleLine = true,
                    isError = false, // Never show as error anymore
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isEnabled) onConfirm(trimmed) },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDuplicate) colorScheme.secondary.copy(alpha = 0.6f) else colorScheme.primary.copy(alpha = 0.60f),
                        unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.10f),
                        focusedLabelColor = if (isDuplicate) colorScheme.secondary.copy(alpha = 0.7f) else colorScheme.primary.copy(alpha = 0.70f),
                        cursorColor = colorScheme.primary,
                    ),
                )

                AnimatedVisibility(
                    visible = isDuplicate && trimmed.isNotEmpty(),
                    enter = fadeIn() + slideInVertically { -4 },
                    exit = fadeOut(),
                ) {
                    Surface(
                        color = colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Found existing record. Proceeding with player history.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(trimmed) },
                enabled = isEnabled,
                modifier = Modifier
                    .drawBehind {
                        if (isEnabled) {
                            val color = if (isDuplicate) colorScheme.secondary else colorScheme.primary
                            drawRoundRect(
                                color = color.copy(alpha = 0.09f),
                                cornerRadius = CornerRadius(12.dp.toPx()),
                            )
                            drawRoundRect(
                                color = color.copy(alpha = 0.30f),
                                cornerRadius = CornerRadius(12.dp.toPx()),
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        }
                    },
            ) {
                Text(
                    text = if (isDuplicate) "Proceed" else "Add",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                    ),
                    color = when {
                        !isEnabled -> colorScheme.onSurface.copy(alpha = 0.25f)
                        isDuplicate -> colorScheme.secondary
                        else -> colorScheme.primary
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
        },
    )
}